import { 
    ProfileAccount, 
    ProfileBid, 
    ProfileMenu, 
    ProfilePoint, 
    ProfileStats, 
    ProfileTransaction 
} from "../components";

const ProfilePage = () => {
    return (
        <div className="min-h-screen bg-gray-50">
            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 lg:gap-8">
                    {/* Left Sidebar */}
                    <aside className="lg:col-span-3">
                        <ProfileMenu />
                    </aside>
                    {/* Main Content */}
                    <section className="lg:col-span-6">
                        <ProfileStats />
                        <ProfileBid />
                        <ProfileTransaction />
                    </section>
                    {/* Right Sidebar */}
                    <aside className="lg:col-span-3">
                        <ProfilePoint />
                        <ProfileAccount />
                    </aside>
                </div>
            </main>
        </div>
    );
};

export default ProfilePage;