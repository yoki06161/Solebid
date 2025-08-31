import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import type { Agreements, SignupErrors, SignupFormData } from '../types/SignupTypes.ts';
import { signupUser } from '../services/UserService';
import { getOAuth2AuthUrl } from '../services/AuthService';

export function useSignup() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState<SignupFormData>({
    email: '', password: '', confirmPassword: '', nickname: '', name: '', phone: '',
  });
  const [agreements, setAgreements] = useState<Agreements>({ all: false, terms: false, privacy: false, marketing: false });
  const [errors, setErrors] = useState<SignupErrors>({
    email: '', password: '', confirmPassword: '', nickname: '', name: '', phone: '', agreeTerms: '', agreePrivacy: ''
  });
  const [showTermsModal, setShowTermsModal] = useState(false);
  const [showPrivacyModal, setShowPrivacyModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const validateEmail = (email: string) => {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email) return '이메일을 입력해주세요.';
    if (!regex.test(email)) return '올바른 이메일 형식이 아닙니다.';
    return '';
  };
  const validatePassword = (password: string) => {
    const regex = /^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\W)(?=\S+$).{8,20}$/;
    if (!password) return '비밀번호를 입력해주세요.';
    if (!regex.test(password)) return '비밀번호는 8~20자 영문, 숫자, 특수문자를 포함해야 합니다.';
    return '';
  };
  const validateNickname = (nickname: string) => {
    if (!nickname) return '닉네임을 입력해주세요.';
    if (nickname.length < 2 || nickname.length > 10) return '닉네임은 2자 이상 10자 이하로 입력해주세요.';
    return '';
  };
  const validatePhone = (phone: string) => {
    const regex = /^01(?:0|1|[6-9])[0-9]{7,8}$/;
    if (!phone) return '휴대폰 번호를 입력해주세요.';
    if (!regex.test(phone)) return '올바른 휴대폰 번호 형식이 아닙니다.';
    return '';
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target as HTMLInputElement & { name: keyof SignupFormData };
    setFormData((prev) => ({ ...prev, [name]: value }));

    let error = '';
    if (name === 'email') error = validateEmail(value);
    if (name === 'password') error = validatePassword(value);
    if (name === 'confirmPassword') error = prevPasswordMatch(value) ? '' : '비밀번호가 일치하지 않습니다.';
    if (name === 'nickname') error = validateNickname(value);
    if (name === 'phone') error = validatePhone(value);
    if (name === 'name' && !value) error = '이름을 입력해주세요.';

    setErrors((prev) => ({ ...prev, [name]: error } as SignupErrors));
  };

  const prevPasswordMatch = (confirm: string) => formData.password === confirm;

  const handleAllAgreements = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { checked } = e.target;
    setAgreements({ all: checked, terms: checked, privacy: checked, marketing: checked });
  };

  const handleAgreementChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, checked } = e.target as HTMLInputElement & { name: keyof Agreements };
    setAgreements((prev) => ({ ...prev, [name]: checked }));
  };

  useEffect(() => {
    const { terms, privacy, marketing } = agreements;
    setAgreements((prev) => ({ ...prev, all: terms && privacy && marketing }));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [agreements.terms, agreements.privacy, agreements.marketing]);

  const isFormValid = useMemo(() => {
    const isFormDataFilled = Object.values(formData).every((v) => String(v).trim() !== '');
    const noErrors = !Object.values(errors).some((e) => e !== '');
    const requiredAgreements = agreements.terms && agreements.privacy;
    return isFormDataFilled && noErrors && requiredAgreements;
  }, [formData, errors, agreements]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const newErrors: SignupErrors = {
      email: validateEmail(formData.email),
      password: validatePassword(formData.password),
      confirmPassword: formData.password !== formData.confirmPassword ? '비밀번호가 일치하지 않습니다.' : '',
      nickname: validateNickname(formData.nickname),
      name: !formData.name ? '이름을 입력해주세요.' : '',
      phone: validatePhone(formData.phone),
      agreeTerms: !agreements.terms ? '이용약관에 동의해주세요.' : '',
      agreePrivacy: !agreements.privacy ? '개인정보 처리방침에 동의해주세요.' : '',
    };
    setErrors(newErrors);

    const valid = Object.values(newErrors).every((err) => err === '');
    if (!valid) return;

    try {
      setSubmitting(true);
      const res = await signupUser({
        email: formData.email,
        password: formData.password,
        nickname: formData.nickname,
        name: formData.name,
        phone: formData.phone,
        marketing: agreements.marketing,
      });
      if (res.success) {
        alert('회원가입이 성공적으로 완료되었습니다!');
        navigate('/');
      } else {
        alert(res.message || '회원가입에 실패했습니다.');
      }
    } catch (err) {
      console.error('Signup error:', err);
      alert('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
    } finally {
      setSubmitting(false);
    }
  };

  const openTermsModal = (e: React.MouseEvent) => { e.preventDefault(); setShowTermsModal(true); };
  const openPrivacyModal = (e: React.MouseEvent) => { e.preventDefault(); setShowPrivacyModal(true); };
  const closeModal = () => { setShowTermsModal(false); setShowPrivacyModal(false); };

  const handleKakaoStart = async () => {
    try {
      const resp = await getOAuth2AuthUrl('kakao');
      if (resp.success && resp.data?.authUrl) {
        window.location.href = resp.data.authUrl;
      } else {
        alert(resp.message || '카카오 로그인 URL 생성에 실패했습니다.');
      }
    } catch (e) {
      alert('카카오 로그인 준비 중 오류가 발생했습니다.');
    }
  };

  return {
    formData, agreements, errors,
    showTermsModal, showPrivacyModal, submitting,
    isFormValid,
    handleInputChange, handleAllAgreements, handleAgreementChange, handleSubmit,
    openTermsModal, openPrivacyModal, closeModal,
    handleKakaoStart,
  };
}

