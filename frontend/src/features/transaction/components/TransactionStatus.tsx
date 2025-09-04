import type { TransactionStatusProps } from '../types/TransactionStatusProps';

const TransactionStatus = ({ status, text }: TransactionStatusProps) => {
    const badgeClassName = `status-badge status-${status}`;
    return (
        <span className={badgeClassName}>
            {text}
        </span>
    );
}

export default TransactionStatus;