import { ProfileAccount, ProfileBid, ProfileInfo, ProfileMenu, ProfileStats, ProfileWish } from "../components";
import ProfilePoint from "../components/ProfilePoint";

const ProfilePage = () => {
    return (
        <div className="min-h-screen bg-gray-50">
            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="grid grid-cols-12 gap-8">
                    {/* Left Sidebar */}
                    <aside className="col-span-3">
                        <ProfileInfo />
                        <ProfileMenu />
                    </aside>
                    {/* Main Content */}
                    <section className="col-span-6">
                        <ProfileStats />
                        <ProfileBid />
                        <ProfileWish />
                    </section>
                    {/* Right Sidebar */}
                    <aside className="col-span-3">
                        <ProfilePoint />
                        <ProfileAccount />
                    </aside>
                </div>
            </main>
        </div>
    );
};

export default ProfilePage;