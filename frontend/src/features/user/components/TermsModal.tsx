import React from 'react';

type Props = {
  open: boolean;
  onClose: () => void;
  title: string;
  children?: React.ReactNode;
};

const TermsModal: React.FC<Props> = ({ open, onClose, title, children }) => {
  if (!open) return null;
  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[80vh] flex flex-col">
        <div className="p-6 border-b border-gray-200">
          <h3 className="text-xl font-bold text-gray-900">{title}</h3>
        </div>
        <div className="p-6 overflow-y-auto flex-1 text-sm text-gray-700 leading-relaxed">
          {children}
        </div>
        <div className="p-6 border-t border-gray-200">
          <button onClick={onClose} className="w-full py-3 bg-blue-500 text-white font-medium !rounded-button hover:bg-blue-600">
            확인
          </button>
        </div>
      </div>
    </div>
  );
};

export default TermsModal;

