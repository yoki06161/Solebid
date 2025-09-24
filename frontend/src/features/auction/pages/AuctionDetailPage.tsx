import React, { useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import { useAuctionDetail } from "../hooks/useAuctionDetail";
import { useAuctionStream } from "../hooks/useAuctionStream";
import { AuctionsApi } from "../services/auctions";
import { idemKey } from "../../../utils/idempotency";
import { toImageUrl } from "../../../utils/toImageUrl";
import type { AuctionStatus } from "../types/auctionDetail";

const krw = (v: number | null | undefined) =>
    v == null
        ? "-"
        : new Intl.NumberFormat("ko-KR", {
            style: "currency",
            currency: "KRW",
            maximumFractionDigits: 0,
        }).format(v);

const pad2 = (n: number) => String(n).padStart(2, "0");

const diffHMS = (iso: string) => {
    const now = Date.now();
    const end = new Date(iso).getTime();
    const d = Math.max(0, end - now);
    const s = Math.floor(d / 1000);
    return {
        hh: Math.floor(s / 3600),
        mm: Math.floor((s % 3600) / 60),
        ss: s % 60,
        finished: d <= 0,
    };
};

const AuctionDetailPage: React.FC = () => {
    const { auctionId: idStr } = useParams<{ auctionId: string }>();
    const auctionId = useMemo(() => (idStr ? Number(idStr) : undefined), [idStr]);

    const { data, setData, loading, err } = useAuctionDetail(auctionId);

    const [activeTab, setActiveTab] = useState<"info" | "bids" | "shipping">("info");
    const [showBidModal, setShowBidModal] = useState(false);
    const [bidAmount, setBidAmount] = useState("");
    const [bidding, setBidding] = useState(false); // 이중 전송 방지

    const images = useMemo(() => data?.product.images ?? [], [data?.product.images]);
    const [currentImageIndex, setCurrentImageIndex] = useState(0);

    useEffect(() => {
        if (!images.length) return;
        const thumbIdx = images.findIndex((i) => i.isThumbnail);
        setCurrentImageIndex(thumbIdx >= 0 ? thumbIdx : 0);
    }, [images]);

    const [clock, setClock] = useState({ hh: 0, mm: 0, ss: 0, finished: false });

    useEffect(() => {
        if (!data?.endAt) return;
        const tick = () => setClock(diffHMS(data.endAt));
        tick();
        const id = window.setInterval(tick, 1000);
        return () => window.clearInterval(id);
    }, [data?.endAt]);

    const currentImageSrc = toImageUrl(images[currentImageIndex]?.filePath || undefined);

    // ----- SSE -----
    useAuctionStream(auctionId, {
        onBid: ({ currentPrice, version }) => {
            setData((prev) => (prev ? { ...prev, currentPrice, version } : prev));
        },
        onExtended: ({ endAt }) => {
            setData((prev) => (prev ? { ...prev, endAt } : prev));
        },
        onStatus: ({ status }) => {
            setData((prev) => (prev ? { ...prev, status: status as AuctionStatus } : prev));
        },
    });

    // ----- 입찰 -----
    const handleBidSubmit: React.FormEventHandler<HTMLFormElement> = async (e) => {
        e.preventDefault();
        if (!auctionId || !data || bidding) return;

        const amount = Number(bidAmount);
        if (!Number.isFinite(amount)) return;

        const base = data.currentPrice ?? data.startPrice;
        const minNext = base + data.tickSize;
        if (amount < minNext) {
            alert(`최소 입찰가는 ${krw(minNext)} 입니다.`);
            return;
        }

        try {
            setBidding(true);
            await AuctionsApi.placeBid(auctionId, amount, idemKey(`bid:${auctionId}`));

            setShowBidModal(false);
            setBidAmount("");

            setTimeout(async () => {
                try {
                    const fresh = await AuctionsApi.getDetail(auctionId);
                    setData(fresh);
                } catch (syncError) {
                    console.error(syncError);
                }
            }, 300);
        } catch (e) {
            console.error(e);
            alert(e instanceof Error ? e.message : "입찰에 실패했습니다.");
        } finally {
            setBidding(false);
        }
    };

    // ----- 로딩/에러 -----
    if (loading) return <div className="min-h-screen flex items-center justify-center">불러오는 중…</div>;
    if (err || !data)
        return <div className="min-h-screen flex items-center justify-center text-red-600">{err ?? "데이터가 없습니다."}</div>;

    // ----- UI -----
    return (
        <div className="min-h-screen bg-gray-50">
            {/* Breadcrumb */}
            <div className="max-w-[1440px] mx-auto px-6 py-4">
                <div className="flex items-center space-x-2 text-sm text-gray-500">
                    <a href="/" className="hover:text-gray-700 cursor-pointer">홈</a>
                    <i className="fas fa-chevron-right text-xs" />
                    <a href="#" className="hover:text-gray-700 cursor-pointer">{data.product.category}</a>
                    <i className="fas fa-chevron-right text-xs" />
                    <span className="text-gray-900">{data.product.name}</span>
                </div>
            </div>

            {/* Product Detail */}
            <div className="max-w-[1440px] mx-auto px-6 pb-12">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-12">
                    {/* Product Images */}
                    <div className="space-y-4">
                        <div className="aspect-square bg-white rounded-lg overflow-hidden flex items-center justify-center">
                            {currentImageSrc ? (
                                <img src={currentImageSrc} alt={data.product.name} className="w-full h-full object-cover object-top" />
                            ) : (
                                <div className="text-gray-400">이미지가 없습니다</div>
                            )}
                        </div>
                        {images.length > 1 && (
                            <div className="grid grid-cols-4 gap-2">
                                {images.map((img, idx) => {
                                    const thumb = toImageUrl(img.filePath) || "";
                                    return (
                                        <button
                                            key={`${img.filePath}-${idx}`}
                                            onClick={() => setCurrentImageIndex(idx)}
                                            className={`aspect-square bg-white rounded-lg overflow-hidden border-2 cursor-pointer ${
                                                currentImageIndex === idx ? "border-blue-500" : "border-gray-200"
                                            }`}
                                        >
                                            <img src={thumb} alt={`Product ${idx + 1}`} className="w-full h-full object-cover object-top" />
                                        </button>
                                    );
                                })}
                            </div>
                        )}
                    </div>

                    {/* Product Info */}
                    <div className="space-y-6">
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900">{data.product.name}</h1>
                            <p className="text-lg text-gray-600 mt-2">
                                {data.product.brand} · {data.product.colorway ?? "-"} · {data.product.size}mm
                            </p>
                        </div>

                        {/* 가격 & 상태 */}
                        <div className="bg-white p-6 rounded-lg shadow-sm">
                            <div className="flex items-center justify-between mb-4">
                                <div>
                                    <div className="mb-3">
                                        <p className="text-sm text-gray-500">시작가</p>
                                        <p className="text-xl font-semibold text-gray-600">{krw(data.startPrice)}</p>
                                    </div>
                                    <div>
                                        <p className="text-sm text-gray-500">현재 입찰가</p>
                                        <p className="text-3xl font-bold text-blue-600">{krw(data.currentPrice)}</p>
                                    </div>
                                </div>
                            </div>

                            {/* 남은 시간 */}
                            <div className="mb-6">
                                <p className="text-sm text-gray-500 mb-1">남은 시간</p>
                                {clock.finished ? (
                                    <div className="text-red-600 font-semibold">경매 종료</div>
                                ) : (
                                    <div className="flex items-center space-x-2">
                                        <TimerBox value={clock.hh} />
                                        <span className="text-gray-400">:</span>
                                        <TimerBox value={clock.mm} />
                                        <span className="text-gray-400">:</span>
                                        <TimerBox value={clock.ss} />
                                    </div>
                                )}
                            </div>

                            {/* 입찰 버튼 */}
                            <button
                                disabled={data.status !== "LIVE" || clock.finished}
                                onClick={() => setShowBidModal(true)}
                                className={`w-full py-4 text-white text-lg font-semibold !rounded-button cursor-pointer whitespace-nowrap ${
                                    data.status !== "LIVE" || clock.finished ? "bg-gray-300" : "bg-blue-500 hover:bg-blue-600"
                                }`}
                            >
                                {data.status === "LIVE" && !clock.finished ? "입찰하기" : "입찰 불가"}
                            </button>

                            {/* 종료 배너 */}
                            {data.status === "ENDED" && (
                                <div className="mt-4 p-4 rounded bg-amber-50 text-amber-800">
                                    경매가 종료되었습니다. 낙찰 결과를 확인해 주세요.
                                    <button
                                        onClick={() => window.location.assign(`/order`)} // TODO: /order/:id 확정시 수정
                                        className="ml-3 px-3 py-1 rounded bg-amber-600 text-white"
                                    >
                                        주문 확인
                                    </button>
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                {/* Tabs */}
                <div className="mt-12">
                    <div className="border-b border-gray-200">
                        <nav className="flex space-x-8">
                            <TabButton active={activeTab === "info"} onClick={() => setActiveTab("info")}>상품정보</TabButton>
                            <TabButton active={activeTab === "bids"} onClick={() => setActiveTab("bids")}>입찰내역</TabButton>
                            <TabButton active={activeTab === "shipping"} onClick={() => setActiveTab("shipping")}>배송/반품 안내</TabButton>
                        </nav>
                    </div>

                    <div className="py-8">
                        {activeTab === "info" && (
                            <div className="bg-white p-6 rounded-lg shadow-sm">
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                                    <InfoRow title="브랜드" value={data.product.brand} />
                                    <InfoRow title="카테고리" value={data.product.category} />
                                    <InfoRow title="모델 코드" value={data.product.modelCode ?? "-"} />
                                    <InfoRow title="색상" value={data.product.colorway ?? "-"} />
                                    <InfoRow title="사이즈" value={`${data.product.size}mm`} />
                                    <InfoRow title="컨디션" value={data.product.condition} />
                                    <InfoRow title="출시일" value={data.product.releaseDate ?? "-"} />
                                    <InfoRow title="호가 단위" value={krw(data.tickSize)} />
                                    <InfoRow title="즉시구매가" value={krw(data.buyoutPrice)} />
                                </div>
                            </div>
                        )}

                        {activeTab === "bids" && (
                            <div className="bg-white rounded-lg shadow-sm overflow-hidden">
                                <div className="px-6 py-4 border-b border-gray-200">
                                    <h4 className="text-lg font-semibold text-gray-900">입찰 내역</h4>
                                </div>
                                <div className="px-6 py-10 text-center text-gray-500">SSE 연동 후 표시됩니다.</div>
                            </div>
                        )}

                        {activeTab === "shipping" && (
                            <div className="bg-white p-6 rounded-lg shadow-sm text-gray-600 space-y-6">
                                <Section title="배송 안내">
                                    <ul className="space-y-1">
                                        <li>• 경매 종료 후 결제 완료 시 1-2일 내 발송</li>
                                        <li>• 전국 무료배송 (제주/도서산간 제외)</li>
                                        <li>• 택배사: CJ대한통운</li>
                                    </ul>
                                </Section>
                                <Section title="반품/교환 안내">
                                    <ul className="space-y-1">
                                        <li>• 수령 후 7일 이내 반품 신청 가능</li>
                                        <li>• 단순 변심 반품 시 배송비 고객 부담</li>
                                        <li>• 상품 하자 시 무료 반품/교환</li>
                                    </ul>
                                </Section>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Bid Modal */}
            {showBidModal && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
                    <div className="bg-white rounded-lg p-8 w-full max-w-md relative">
                        <button
                            onClick={() => setShowBidModal(false)}
                            className="absolute top-4 right-4 text-gray-500 hover:text-gray-700 cursor-pointer"
                            aria-label="닫기"
                        >
                            <i className="fas fa-times" />
                        </button>
                        <h2 className="text-2xl font-bold text-gray-900 mb-6">입찰하기</h2>
                        <div className="mb-6">
                            <p className="text-sm text-gray-500">현재 최고가</p>
                            <p className="text-2xl font-bold text-blue-600">{krw(data.currentPrice)}</p>
                            <p className="text-xs text-gray-500 mt-1">최소 입찰 단위: {krw(data.tickSize)}</p>
                        </div>
                        <form onSubmit={handleBidSubmit} className="space-y-4">
                            <div>
                                <label htmlFor="bidAmount" className="block text-sm font-medium text-gray-700 mb-1">입찰가</label>
                                <input
                                    type="number"
                                    id="bidAmount"
                                    value={bidAmount}
                                    onChange={(e) => setBidAmount(e.target.value)}
                                    min={(data.currentPrice ?? data.startPrice) + data.tickSize}
                                    step={data.tickSize}
                                    placeholder={String(Math.ceil(((data.currentPrice ?? data.startPrice) + data.tickSize) / 10) * 10)}
                                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-lg
                    [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                                    required
                                />
                                <p className="text-xs text-gray-500 mt-1">
                                    최소 입찰가: {krw((data.currentPrice ?? data.startPrice) + data.tickSize)}
                                </p>
                            </div>
                            <button
                                type="submit"
                                disabled={bidding}
                                className="w-full py-3 bg-blue-500 text-white text-lg font-semibold !rounded-button hover:bg-blue-600 disabled:opacity-50"
                            >
                                {bidding ? "입찰 중..." : "입찰하기"}
                            </button>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

// ----- 서브 컴포넌트 -----
const TimerBox: React.FC<{ value: number }> = ({ value }) => (
    <div className="bg-red-500 text-white px-3 py-1 rounded text-lg font-mono">{pad2(value)}</div>
);

const InfoRow: React.FC<{ title: string; value: string }> = ({ title, value }) => (
    <div className="flex justify-between">
        <span className="text-gray-600">{title}</span>
        <span className="text-gray-900">{value}</span>
    </div>
);

const Section: React.FC<{ title: string; children: React.ReactNode }> = ({ title, children }) => (
    <div>
        <h4 className="text-lg font-semibold text-gray-900 mb-4">{title}</h4>
        {children}
    </div>
);

const TabButton: React.FC<{ active: boolean; onClick: () => void; children: React.ReactNode }> = ({
                                                                                                      active,
                                                                                                      onClick,
                                                                                                      children,
                                                                                                  }) => (
    <button
        onClick={onClick}
        className={`py-4 px-1 border-b-2 font-medium text-sm cursor-pointer ${
            active ? "border-blue-500 text-blue-600" : "border-transparent text-gray-500 hover:text-gray-700"
        }`}
    >
        {children}
    </button>
);

export default AuctionDetailPage;
