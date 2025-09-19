import { useEffect, useState } from "react";
import { parseTimeToMinutes } from "../../../utils/time-utils";
import { NotificationList, NotificationNav, NotificationPush, NotificationSearch } from "../components";
import { notices, tabs } from "../components/mockData";
import type { Notification } from "../types/Notification";

const NotificationPage = () => {
    const [activeTab, setActiveTab] = useState("전체");
    const [sortBy, setSortBy] = useState("마감임박순");
    const [searchQuery, setSearchQuery] = useState("");
    const [showNotification, setShowNotification] = useState(false);
    const [lastNotification, setLastNotification] = useState(null);
    const [notifications, setNotifications] = useState(notices);

    const filteredNotifications = notifications.filter((notification) => {
        const matchesTab = activeTab === "전체" || notification.category === activeTab;
        const matchesSearch = notification.title
            .toLowerCase()
            .includes(searchQuery.toLowerCase());
        return matchesTab && matchesSearch;
    });

    const sortFunctions: { [key: string]: (a: Notification, b: Notification) => number } = {
        // 마감임박순: timeLeft를 분으로 환산하여 오름차순 정렬
        "마감임박순": (a, b) => parseTimeToMinutes(a.timeLeft) - parseTimeToMinutes(b.timeLeft),
        // 기본 정렬: id를 기준으로 내림차순 정렬
        "default": (a, b) => b.id - a.id,
    };

    const compareFunction = sortFunctions[sortBy] || sortFunctions.default;

    const sortedNotifications = [...filteredNotifications].sort(compareFunction);

    // 알림을 숨기는 타이머를 관리하기 위한 useEffect
    useEffect(() => {
        if (showNotification) {
            const timer = setTimeout(() => {
                setShowNotification(false);
            }, 5000);

            return () => clearTimeout(timer);
        }
    }, [showNotification]);

    // 입찰가 시뮬레이션 및 알림 트리거를 위한 useEffect
    useEffect(() => {
        const interval = setInterval(() => {
            let outbidNotification = null;

            setNotifications((prevNotifications) => {
                const newNotifications = prevNotifications.map((notification) => {
                    const oldBid = notification.currentBid;
                    const newBid = oldBid + Math.floor(Math.random() * 50000);

                    // 내가 입찰했고, 새로운 입찰가가 나의 입찰가를 넘었는지 확인
                    const wasOutbid =
                        notification.myBid &&
                        newBid > notification.myBidAmount &&
                        newBid !== oldBid;

                    if (wasOutbid) {
                        // 사이드 이펙트를 바로 실행하지 않고, 해당 알림 정보만 저장
                        outbidNotification = {
                            ...notification,
                            currentBid: newBid,
                            previousBid: oldBid,
                        };
                    }

                    return {
                        ...notification,
                        currentBid: newBid,
                    };
                });

                return newNotifications;
            });

            // 상태 업데이트가 끝난 후, 저장해 둔 정보를 바탕으로 사이드 이펙트 실행
            if (outbidNotification) {
                setLastNotification(outbidNotification);
                setShowNotification(true);
            }
        }, 5000);

        return () => clearInterval(interval);
    }, []);

    // TODO: 입찰 화면 이동
    const navigateToAuction = (id: number) => {
        console.log(`Navigating to auction page for item ${id}`);
    };

    return (
        <div className="min-h-screen bg-gray-50">
            {showNotification && lastNotification && (
                <NotificationPush
                    notification={lastNotification}
                    onClose={() => setShowNotification(false)}
                />
            )}
            <NotificationNav
                tabs={tabs}
                activeTab={activeTab}
                onTabChange={setActiveTab}
            />
            <NotificationSearch
                sortBy={sortBy}
                onSortChange={setSortBy}
                searchQuery={searchQuery}
                onSearchChange={setSearchQuery}
            />
            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    <NotificationList
                        notifications={sortedNotifications}
                        onNavigate={navigateToAuction}
                    />
                </div>
                {sortedNotifications.length > 0 && (
                    <div className="text-center mt-8">
                        <button
                            onClick={() => { }}
                            className="bg-gray-100 text-gray-700 py-2 px-6 rounded-md hover:bg-gray-200 transition-colors duration-200 !rounded-button whitespace-nowrap cursor-pointer"
                        >
                            더 많은 경매 보기
                        </button>
                    </div>
                )}
            </main>
        </div>
    );
};

export default NotificationPage;