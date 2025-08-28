import React from 'react';
import type { CartListProps } from '../types/CartListProps';
import CartItem from './CartItem';

const CartList: React.FC<CartListProps> = ({ items, isEditing, onToggleEdit, onRemoveItem }) => {
    return (
        <div className='flex flex-col p-4 space-y-4'>
            <button
                onClick={onToggleEdit}
                className="self-end text-blue-600 font-medium cursor-pointer whitespace-nowrap"
            >
                {isEditing ? "완료" : "편집"}
            </button>
            {items.map((item) => (
                <CartItem
                    key={item.id}
                    item={item}
                    isEditing={isEditing}
                    onRemoveItem={onRemoveItem}
                />
            ))}
        </div>
    )
}

export default CartList;