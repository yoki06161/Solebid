export interface OrderDetailTimeline {
  status: string;
  date: string;
  completed: boolean;
}

export interface OrderDetailTimelineProps {
  timeline: OrderDetailTimeline[];
}