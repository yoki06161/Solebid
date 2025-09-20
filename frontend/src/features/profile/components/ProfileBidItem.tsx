import type { ProfileBidItemProps } from "../types/ProfileBidItemProps";

const ProfileBidItem = ({ name, date, price, imageUrl }: ProfileBidItemProps) => {
    return (
        <div className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
            <div className="flex items-center">
                <img
                    src={imageUrl}
                    alt={name}
                    className="w-12 h-12 rounded-lg object-cover mr-4" />
                <div>
                    <h4 className="font-medium text-gray-900">
                        {name}
                    </h4>
                    <p className="text-gray-600 text-sm">
                        {date}
                    </p>
                </div>
            </div>
            <div className="text-right">
                <div className="font-semibold text-gray-900">
                    {price}
                </div>
            </div>
        </div>
    );
}

export default ProfileBidItem;