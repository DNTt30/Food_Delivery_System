export interface OrderItem {
  id: string;
  foodName: string;
  quantity: number;
  price: number;
  imageUrl: string;
}

export type OrderStatus = 'PENDING' | 'PREPARING' | 'DELIVERING' | 'COMPLETED' | 'CANCELLED';

export interface Review {
  id: string;
  orderId: string;
  rating: number;
  comment: string;
  imageUrl?: string;
  restaurantReply?: string;
  repliedAt?: string;
}

export interface Order {
  id: string;
  restaurantName: string;
  restaurantAddress: string;
  items: OrderItem[];
  totalPrice: number;
  status: OrderStatus;
  createdAt: string;
  review?: Review;
}

export interface Notification {
  id: string;
  title: string;
  content: string;
  type: 'NEW_REVIEW' | 'REPLY';
  orderId: string;
  isRead: boolean;
  createdAt: string;
}

export interface CodeSnippet {
  title: string;
  description: string;
  language: string;
  code: string;
}
