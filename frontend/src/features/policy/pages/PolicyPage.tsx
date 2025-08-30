import ReactMarkdown from 'react-markdown';
import privacyPolicyContent from '../../../assets/privacy-policy.md?raw';

const PolicyPage = () => {
    return (
        <article className='prose max-w-none p-6 bg-white rounded-lg shadow-md'>
            <ReactMarkdown>
                {privacyPolicyContent}
            </ReactMarkdown>
        </article>
    )
}

export default PolicyPage;
