import React, { useCallback } from "react";
import { NotificationList } from "../components";
import { useNotificationData } from "../hook/useNotificationData";
import { useUserNotificationStream } from "../hook/useUserNotificationStream";

// ₩ 포맷 유틸
const krw = (v: string | number) =>
    Number(v).toLocaleString("ko-KR", { maximumFractionDigits: 0 });

const NotificationPage: React.FC = () => {
    const {
        list,
        hasMore,
        loading,
        unread,
        loadMore,
        loadFirst,
        markRead,
        markAllRead,
        setUnread,
    } = useNotificationData();

    useUserNotificationStream({
        onBadge: (d) => setUnread(d.unreadCount),

        // 내가 밀렸을 때
        onOutbid: (d) => {
            alert(
                `입찰가 갱신: ${d.productName}\n` +
                `현재가: ${krw(d.currentPrice)}원\n` +
                `내 입찰가: ${krw(d.myBid)}원`
            );
            // 최신 알림 반영
            loadFirst();
        },

        // 낙찰 성공
        onAuctionWon: (d) => {
            loadFirst();
            alert(`낙찰 성공: ${d.productName}\n최종가: ${krw(d.finalPrice)}원`);
        },

        // 낙찰 실패
        onAuctionLost: (d) => {
            loadFirst();
            alert(`낙찰 실패: ${d.productName}\n최종가: ${krw(d.finalPrice)}원`);
        },
    });

    const onItemClick = useCallback(
        (id: number, link?: string | null) => {
            markRead(id);
            if (link) window.location.assign(link); // history 남기고 이동
        },
        [markRead]
    );

    return (
        <div className="min-h-screen bg-gray-50">
            <header className="bg-white shadow-sm border-b">
                <div className="max-w-7xl mx-auto px-4 flex justify-between items-center h-16">
                    <h1 className="text-2xl font-bold text-gray-900">내 알림</h1>
                    <div className="flex items-center gap-3">
                        <button onClick={loadFirst} className="px-3 py-1.5 border rounded-md">
                            새로고침
                        </button>
                        <button onClick={markAllRead} className="px-3 py-1.5 border rounded-md">
                            모두 읽음
                        </button>
                        <div className="relative">
                            {unread > 0 && (
                                <span className="absolute -top-2 -right-2 bg-red-600 text-white text-xs rounded-full px-1.5 py-0.5">
                  {unread}
                </span>
                            )}
                        </div>
                    </div>
                </div>
            </header>

            <main className="max-w-7xl mx-auto px-4 py-8">
                <NotificationList items={list} onItemClick={onItemClick} />
                {hasMore && (
                    <div className="text-center mt-8">
                        <button
                            onClick={loadMore}
                            disabled={loading}
                            className="bg-gray-100 text-gray-700 py-2 px-6 rounded-md hover:bg-gray-200 disabled:opacity-60"
                        >
                            {loading ? "불러오는 중..." : "더 불러오기"}
                        </button>
                    </div>
                )}
            </main>
        </div>
    );
};

export default NotificationPage;
