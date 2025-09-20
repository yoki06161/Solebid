import { useState } from "react";
import { Link } from "react-router-dom";
import { menuData, type MenuDataItem } from "./mockData";
import ProfileEditModal from "./ProfileEditModal";
import SensitiveProfileEditModal from "./SensitiveProfileEditModal";
import PasswordChangeModal from "./PasswordChangeModal";

const ProfileMenu = () => {
    const [isProfileEditModalOpen, setIsProfileEditModalOpen] = useState(false);
    const [isSensitiveEditModalOpen, setIsSensitiveEditModalOpen] = useState(false);
    const [isPasswordChangeModalOpen, setIsPasswordChangeModalOpen] = useState(false);

    const handleMenuClick = (link: MenuDataItem) => {
        if (link.action === "profile-edit") {
            setIsProfileEditModalOpen(true);
        } else if (link.action === "sensitive-edit") {
            setIsSensitiveEditModalOpen(true);
        } else if (link.action === "password-change") {
            setIsPasswordChangeModalOpen(true);
        }
    };

    const handleCloseModal = () => {
        setIsProfileEditModalOpen(false);
        setIsSensitiveEditModalOpen(false);
        setIsPasswordChangeModalOpen(false);
    };

    return (
        <>
            <div className="bg-white rounded-lg shadow-sm p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">
                    퀵 메뉴
                </h3>
                <nav className="space-y-2">
                    {menuData.map((link, index) => {
                        if (link.action && ["profile-edit", "sensitive-edit", "password-change"].includes(link.action)) {
                            return (
                                <button
                                    key={index}
                                    onClick={() => handleMenuClick(link)}
                                    className="flex items-center px-3 py-2 text-gray-700 rounded-lg hover:bg-gray-100 cursor-pointer w-full text-left"
                                >
                                    <i className={`${link.icon} w-5 text-center mr-3`} />
                                    {link.text}
                                </button>
                            );
                        }
                        
                        return (
                            <Link
                                key={index}
                                to={link.href}
                                className="flex items-center px-3 py-2 text-gray-700 rounded-lg hover:bg-gray-100 cursor-pointer"
                            >
                                <i className={`${link.icon} w-5 text-center mr-3`} />
                                {link.text}
                            </Link>
                        );
                    })}
                </nav>
            </div>

            <ProfileEditModal
                open={isProfileEditModalOpen}
                onClose={handleCloseModal}
                onSuccess={() => {
                    // 프로필 업데이트 성공 시 추가 작업이 필요하면 여기에 구현
                }}
            />

            <SensitiveProfileEditModal
                open={isSensitiveEditModalOpen}
                onClose={handleCloseModal}
                onSuccess={() => {
                    // 민감한 정보 업데이트 성공 시 추가 작업이 필요하면 여기에 구현
                }}
            />

            <PasswordChangeModal
                open={isPasswordChangeModalOpen}
                onClose={handleCloseModal}
                onSuccess={() => {
                    // 비밀번호 변경 성공 시 추가 작업이 필요하면 여기에 구현
                }}
            />
        </>
    );
};

export default ProfileMenu;