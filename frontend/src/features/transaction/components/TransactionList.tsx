import type { TransactionListProps } from "../types/TransactionListProps";
import TransactionItem from "./TransactionItem";

const TransactionList = ({ data }: TransactionListProps) => {
    return (
        <div className="bg-white rounded-lg shadow-sm">
            <div className="p-6 border-b border-gray-200">
                <h2 className="text-lg font-semibold text-gray-900">
                    판매 내역 ({data.length}건)
                </h2>
            </div>
            <div className="divide-y divide-gray-200">
                {data.length > 0
                    ? (
                        data.map((item) =>
                            <TransactionItem
                                key={item.id}
                                item={item}
                            />
                        )
                    )
                    : (
                        <div className="p-12 text-center">
                            <i className="fas fa-search text-gray-300 text-4xl mb-4" />
                            <h3 className="text-lg font-medium text-gray-900 mb-2">
                                검색 결과가 없습니다
                            </h3>
                            <p className="text-gray-600">
                                다른 검색어나 필터를 사용해보세요.
                            </p>
                        </div>
                    )}
            </div>
        </div>
    );
};

export default TransactionList;