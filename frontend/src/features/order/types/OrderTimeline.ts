export interface OrderTimeline {
    status: string;
    date: string;
    completed: boolean;
}

export interface OrderDetailTimelineProps {
    timeline: OrderTimeline[];
}