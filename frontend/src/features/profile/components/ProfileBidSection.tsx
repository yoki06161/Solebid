import { Link } from "react-router-dom";
import type { ProfileBidSectionProps } from "../types/ProfileBidSectionProps";

const ProfileBidSection = ({ title, linkTo, linkText, children }: ProfileBidSectionProps) => {
    return (
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
            <div className="flex justify-between items-center mb-4">
                <h3 className="text-lg font-semibold text-gray-900">
                    {title}
                </h3>
                {linkTo && linkText && (
                    <Link
                        to={linkTo}
                        className="text-blue-600 text-sm hover:text-blue-800 cursor-pointer">
                        {linkText}
                    </Link>
                )}
            </div>
            {children}
        </div>
    );
};

export default ProfileBidSection;