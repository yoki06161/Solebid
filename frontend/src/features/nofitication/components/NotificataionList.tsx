import { Fragment } from "react";
import type { NotificationListProps } from "../types/NotificationListProps";
import NotificationItem from "./NotificationItem";

const NotificationList = ({ notifications, onNavigate }: NotificationListProps) => {
    if (notifications.length === 0) {
        return (
            <div className="text-center py-12 col-span-full">
                <i className="fas fa-search text-4xl text-gray-300 mb-4" />
                <p className="text-gray-500">
                    검색 결과가 없습니다.
                </p>
            </div>
        );
    }

    return (
        <Fragment>
            {notifications.map((notification) => (
                <NotificationItem
                    key={notification.id}
                    notification={notification}
                    onNavigate={onNavigate}
                />
            ))}
        </Fragment>
    );
};

export default NotificationList;