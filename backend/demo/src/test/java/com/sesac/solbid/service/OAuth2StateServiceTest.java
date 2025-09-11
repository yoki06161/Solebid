package com.sesac.solbid.service;

import com.sesac.solbid.exception.OAuth2StateException;
import com.sesac.solbid.service.auth.InMemoryOAuth2StateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * OAuth2StateService 단위 테스트
 * State 생성, 검증, 삭제 및 만료 처리 로직 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2StateService 단위 테스트")
class OAuth2StateServiceTest {

    private InMemoryOAuth2StateService stateService;

    @BeforeEach
    void setUp() {
        stateService = new InMemoryOAuth2StateService();
    }

    @Test
    @DisplayName("State 생성 성공 테스트")
    void generateState_Success() {
        // When
        String state = stateService.generateState();

        // Then
        assertThat(state).isNotNull();
        assertThat(state).isNotEmpty();
        assertThat(state).hasSize(36); // UUID 형식: 8-4-4-4-12 = 36자
        assertThat(state).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
        
        // State가 내부 저장소에 저장되었는지 확인
        assertThat(stateService.getStateCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("State 생성 시 고유성 보장 테스트")
    void generateState_Uniqueness() {
        // When
        String state1 = stateService.generateState();
        String state2 = stateService.generateState();
        String state3 = stateService.generateState();

        // Then
        assertThat(state1).isNotEqualTo(state2);
        assertThat(state2).isNotEqualTo(state3);
        assertThat(state1).isNotEqualTo(state3);
        assertThat(stateService.getStateCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("유효한 State 검증 성공 테스트")
    void validateState_Success() {
        // Given
        String state = stateService.generateState();

        // When & Then
        assertThatCode(() -> stateService.validateState(state))
                .doesNotThrowAnyException();
        
        boolean result = stateService.validateState(state);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("null State 검증 실패 테스트")
    void validateState_Fail_NullState() {
        // When & Then
        assertThatThrownBy(() -> stateService.validateState(null))
                .isInstanceOf(OAuth2StateException.class);
    }

    @Test
    @DisplayName("빈 문자열 State 검증 실패 테스트")
    void validateState_Fail_EmptyState() {
        // When & Then
        assertThatThrownBy(() -> stateService.validateState(""))
                .isInstanceOf(OAuth2StateException.class);
        
        assertThatThrownBy(() -> stateService.validateState("   "))
                .isInstanceOf(OAuth2StateException.class);
    }

    @Test
    @DisplayName("존재하지 않는 State 검증 실패 테스트")
    void validateState_Fail_NonExistentState() {
        // Given
        String nonExistentState = "non-existent-state-12345";

        // When & Then
        assertThatThrownBy(() -> stateService.validateState(nonExistentState))
                .isInstanceOf(OAuth2StateException.class);
    }

    @Test
    @DisplayName("만료된 State 검증 실패 테스트")
    void validateState_Fail_ExpiredState() throws Exception {
        // Given
        String state = stateService.generateState();
        
        // 리플렉션을 사용하여 state를 만료시킴
        expireState(state);

        // When & Then
        assertThatThrownBy(() -> stateService.validateState(state))
                .isInstanceOf(OAuth2StateException.class);
        
        // 만료된 state는 자동으로 제거되어야 함
        assertThat(stateService.getStateCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("State 삭제 성공 테스트")
    void removeState_Success() {
        // Given
        String state = stateService.generateState();
        assertThat(stateService.getStateCount()).isEqualTo(1);

        // When
        stateService.removeState(state);

        // Then
        assertThat(stateService.getStateCount()).isEqualTo(0);
        
        // 삭제된 state는 검증 실패해야 함
        assertThatThrownBy(() -> stateService.validateState(state))
                .isInstanceOf(OAuth2StateException.class);
    }

    @Test
    @DisplayName("null State 삭제 시 예외 없음 테스트")
    void removeState_NullState_NoException() {
        // When & Then
        assertThatCode(() -> stateService.removeState(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("존재하지 않는 State 삭제 시 예외 없음 테스트")
    void removeState_NonExistentState_NoException() {
        // When & Then
        assertThatCode(() -> stateService.removeState("non-existent-state"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("만료된 State 정리 테스트")
    void cleanupExpiredStates_Success() throws Exception {
        // Given
        String validState = stateService.generateState();
        String expiredState1 = stateService.generateState();
        String expiredState2 = stateService.generateState();
        
        // 일부 state를 만료시킴
        expireState(expiredState1);
        expireState(expiredState2);
        
        assertThat(stateService.getStateCount()).isEqualTo(3);

        // When
        stateService.cleanupExpiredStates();

        // Then
        assertThat(stateService.getStateCount()).isEqualTo(1);
        
        // 유효한 state는 여전히 검증 가능해야 함
        assertThatCode(() -> stateService.validateState(validState))
                .doesNotThrowAnyException();
        
        // 만료된 state들은 검증 실패해야 함
        assertThatThrownBy(() -> stateService.validateState(expiredState1))
                .isInstanceOf(OAuth2StateException.class);
        assertThatThrownBy(() -> stateService.validateState(expiredState2))
                .isInstanceOf(OAuth2StateException.class);
    }

    @Test
    @DisplayName("State 개수 조회 테스트")
    void getStateCount_Test() {
        // Given
        assertThat(stateService.getStateCount()).isEqualTo(0);

        // When
        stateService.generateState();
        stateService.generateState();
        stateService.generateState();

        // Then
        assertThat(stateService.getStateCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("State 사용 후 즉시 삭제 패턴 테스트")
    void stateUsagePattern_ValidateAndRemove() {
        // Given
        String state = stateService.generateState();

        // When - 검증 후 즉시 삭제 (일반적인 사용 패턴)
        boolean isValid = stateService.validateState(state);
        stateService.removeState(state);

        // Then
        assertThat(isValid).isTrue();
        assertThat(stateService.getStateCount()).isEqualTo(0);
        
        // 삭제 후 재검증은 실패해야 함
        assertThatThrownBy(() -> stateService.validateState(state))
                .isInstanceOf(OAuth2StateException.class);
    }

    @Test
    @DisplayName("동시성 테스트 - 여러 State 동시 생성")
    void concurrency_MultipleStateGeneration() throws InterruptedException {
        // Given
        int threadCount = 10;
        int statesPerThread = 10;
        Thread[] threads = new Thread[threadCount];

        // When
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < statesPerThread; j++) {
                    stateService.generateState();
                }
            });
            threads[i].start();
        }

        // 모든 스레드 완료 대기
        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        assertThat(stateService.getStateCount()).isEqualTo(threadCount * statesPerThread);
    }

    @Test
    @DisplayName("State 마스킹 기능 테스트")
    void maskState_Test() throws Exception {
        // Given
        String state = stateService.generateState();
        
        // 리플렉션으로 private 메서드 접근
        Method maskStateMethod = InMemoryOAuth2StateService.class.getDeclaredMethod("maskState", String.class);
        maskStateMethod.setAccessible(true);

        // When
        String maskedState = (String) maskStateMethod.invoke(stateService, state);

        // Then
        assertThat(maskedState).isNotEqualTo(state);
        assertThat(maskedState).startsWith(state.substring(0, 4));
        assertThat(maskedState).endsWith(state.substring(state.length() - 4));
        assertThat(maskedState).contains("****");
    }

    @Test
    @DisplayName("짧은 State 마스킹 테스트")
    void maskState_ShortState_Test() throws Exception {
        // Given
        Method maskStateMethod = InMemoryOAuth2StateService.class.getDeclaredMethod("maskState", String.class);
        maskStateMethod.setAccessible(true);

        // When & Then
        String maskedNull = (String) maskStateMethod.invoke(stateService, (String) null);
        assertThat(maskedNull).isEqualTo("****");

        String maskedShort = (String) maskStateMethod.invoke(stateService, "short");
        assertThat(maskedShort).isEqualTo("****");
    }

    /**
     * 리플렉션을 사용하여 특정 state를 만료시키는 헬퍼 메서드
     */
    private void expireState(String state) throws Exception {
        Field stateStoreField = InMemoryOAuth2StateService.class.getDeclaredField("stateStore");
        stateStoreField.setAccessible(true);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> stateStore = (Map<String, Object>) stateStoreField.get(stateService);
        
        Object stateInfo = stateStore.get(state);
        if (stateInfo != null) {
            // StateInfo의 expiryTime을 과거로 설정
            Field expiryTimeField = stateInfo.getClass().getDeclaredField("expiryTime");
            expiryTimeField.setAccessible(true);
            expiryTimeField.set(stateInfo, LocalDateTime.now().minusMinutes(1));
        }
    }
}