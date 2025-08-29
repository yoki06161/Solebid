import { Fragment, useState } from "react";
import Modal from "../../../components/Modal";

const PointConvertFormat = ({ onClose }: { onClose: () => void }) => {
    const handleConvert = () => {
        alert('포인트 전환이 완료되었습니다.');
        onClose();
    }
    return (
        <Fragment>
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
                현금을 포인트로 전환
            </h3>
            <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                    전환할 금액
                </label>
                <div className="relative">
                    <input
                        type="number"
                        placeholder="금액을 입력하세요"
                        min="1000"
                        step="1000"
                        className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <span className="absolute right-10 top-2 text-gray-500">
                        원
                    </span>
                </div>
                <p className="text-sm text-gray-500 mt-1">
                    최소 1,000원부터 전환 가능
                </p>
            </div>
            <div className="flex justify-end space-x-3">
                <button
                    onClick={onClose}
                    className="px-4 py-2 text-gray-600 hover:text-gray-900 !rounded-button whitespace-nowrap">
                    취소
                </button>
                <button
                    onClick={handleConvert}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 !rounded-button whitespace-nowrap">
                    전환하기
                </button>
            </div>
        </Fragment>
    );
};

const ProfilePoint = () => {
    const [isModalOpen, setIsModalOpen] = useState(false);
    return (
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
                포인트 & 혜택
            </h3>
            <div className="space-y-4">
                <div className="flex justify-between items-center p-3 bg-blue-50 rounded-lg">
                    <div>
                        <div className="font-medium text-gray-900">
                            적립 포인트
                        </div>
                        <div className="text-blue-600 font-semibold">
                            2,450P
                        </div>
                    </div>
                    <i className="fas fa-coins text-blue-600 text-xl" />
                </div>
                <button
                    onClick={() => setIsModalOpen(true)}
                    className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 !rounded-button whitespace-nowrap">
                    <i className="fas fa-exchange-alt mr-2" />
                    현금을 포인트로 전환
                </button>
            </div>
            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}>
                <PointConvertFormat onClose={() => setIsModalOpen(false)} />
            </Modal>
        </div>
    );
};

export default ProfilePoint;