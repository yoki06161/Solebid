import { convertToBidItemProps } from "../../../utils/bid-utils";
import { useBidWinning } from "../hooks/useBidWinning";
import { useImageUrls } from "../../../hooks/useProductImageUrls";
import ProfileBidItem from "./ProfileBidItem";
import { ProfileBidEmpty, ProfileBidError, ProfileBidLoading } from "./ProfileBidStates";
import ProfileBidSection from "./ProfileBidSection";

const TITLE = "최근 낙찰 내역";
const MAX_DISPLAY_COUNT = 3;

const ProfileBid = () => {
    const { winningBids, loading, error } = useBidWinning();
    
    // 기본 추출 함수 사용 (productImageUrl 필드를 자동으로 찾음)
    const { itemsWithImages: bidsWithImages, isLoadingImages } = useImageUrls(winningBids);

    if (loading || isLoadingImages) {
        return <ProfileBidLoading title={TITLE} />;
    }

    if (error) {
        return <ProfileBidError title={TITLE} error={error} />;
    }

    return (
        <ProfileBidSection
            title={TITLE}
            linkTo="/cart"
            linkText="전체 보기"
        >
            <div className="space-y-4">
                {bidsWithImages.length === 0 ? (
                    <ProfileBidEmpty />
                ) : (
                    bidsWithImages.slice(0, MAX_DISPLAY_COUNT).map(bid => (
                        <ProfileBidItem
                            key={bid.bidId}
                            {...convertToBidItemProps(bid)}
                        />
                    ))
                )}
            </div>
        </ProfileBidSection>
    );
};

export default ProfileBid;