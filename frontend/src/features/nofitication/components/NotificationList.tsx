import React from "react";
import type { ServerNotification } from "../api";

type Props = {
    items: ServerNotification[];
    onItemClick?: (id: number, link?: string | null) => void;
};

const fmtDate = (iso: string) =>
    new Date(iso).toLocaleString("ko-KR", { hour12: false });

const NotificationList: React.FC<Props> = ({ items, onItemClick }) => {
    return (
        <div className="space-y-4">
            {items.map((n) => (
                <div
                    key={n.notificationId}
                    onClick={() => onItemClick?.(n.notificationId, n.linkUrl ?? null)}
                    className={`bg-white rounded-lg shadow-sm border p-4 hover:shadow-md transition-shadow duration-200 cursor-pointer ${
                        n.isRead ? "opacity-80" : ""
                    }`}
                >
                    <div className="flex justify-between items-start gap-4">
                        <div className="space-y-1 min-w-0">
                            <div className="flex items-center gap-2">
                                <h3 className="font-medium text-gray-900 truncate">{n.title}</h3>
                                {!n.isRead && (
                                    <span className="bg-blue-100 text-blue-800 text-xs px-2 py-0.5 rounded-full shrink-0">
                    새 알림
                  </span>
                                )}
                            </div>
                            <p className="text-sm text-gray-600 line-clamp-2">{n.content}</p>
                            <div className="text-xs text-gray-400">{fmtDate(n.createAt)}</div>
                        </div>
                        <div className="text-right shrink-0">
              <span className="text-xs rounded px-2 py-1 border">
                {n.notificationType}
              </span>
                        </div>
                    </div>
                </div>
            ))}

            {items.length === 0 && (
                <div className="text-center py-12">
                    <i className="fas fa-inbox text-4xl text-gray-300 mb-4" />
                    <p className="text-gray-500">알림이 없습니다.</p>
                </div>
            )}
        </div>
    );
};

export default NotificationList;
