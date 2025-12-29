package org.example.restaurant.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Nested
    @DisplayName("ValidationException Tests")
    class ValidationExceptionTests {
        @Test
        @DisplayName("ValidationException - 單參數建構子")
        void validationException_SingleArgConstructor() {
            ValidationException ex = new ValidationException("Test message");
            assertEquals("Test message", ex.getMessage());
            assertNull(ex.getField());
            assertEquals("VALIDATION_ERROR", ex.getErrorCode());
        }

        @Test
        @DisplayName("ValidationException - 雙參數建構子")
        void validationException_TwoArgConstructor() {
            ValidationException ex = new ValidationException("Test message", "fieldName");
            assertEquals("Test message", ex.getMessage());
            assertEquals("fieldName", ex.getField());
            assertEquals("VALIDATION_ERROR", ex.getErrorCode());
        }

        @Test
        @DisplayName("ValidationException - 三參數建構子")
        void validationException_ThreeArgConstructor() {
            ValidationException ex = new ValidationException("Test message", "fieldName", "CUSTOM_ERROR");
            assertEquals("Test message", ex.getMessage());
            assertEquals("fieldName", ex.getField());
            assertEquals("CUSTOM_ERROR", ex.getErrorCode());
        }

        @Test
        @DisplayName("ValidationException - getField 返回欄位名稱")
        void validationException_GetField() {
            ValidationException ex = new ValidationException("message", "testField");
            assertEquals("testField", ex.getField());
        }

        @Test
        @DisplayName("ValidationException - getErrorCode 返回錯誤碼")
        void validationException_GetErrorCode() {
            ValidationException ex = new ValidationException("message", "field", "MY_ERROR");
            assertEquals("MY_ERROR", ex.getErrorCode());
        }

        @Test
        @DisplayName("ValidationException - toString 包含訊息")
        void validationException_ToString_ContainsMessage() {
            ValidationException ex = new ValidationException("Test error message");
            String result = ex.toString();
            assertTrue(result.contains("Test error message"));
            assertTrue(result.contains("ValidationException"));
            assertTrue(result.contains("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("ValidationException - toString 包含欄位")
        void validationException_ToString_ContainsField() {
            ValidationException ex = new ValidationException("Error", "username");
            String result = ex.toString();
            assertTrue(result.contains("field='username'"));
        }

        @Test
        @DisplayName("ValidationException - toString 無欄位時不包含欄位")
        void validationException_ToString_NoField() {
            ValidationException ex = new ValidationException("Error");
            String result = ex.toString();
            assertFalse(result.contains("field='"));
            assertTrue(result.contains("message='Error'"));
        }

        @Test
        @DisplayName("ValidationException - toString 包含自訂錯誤碼")
        void validationException_ToString_ContainsCustomErrorCode() {
            ValidationException ex = new ValidationException("Error", "field", "CUSTOM_CODE");
            String result = ex.toString();
            assertTrue(result.contains("errorCode='CUSTOM_CODE'"));
        }

        @Test
        @DisplayName("ValidationException - 是 RuntimeException 子類")
        void validationException_IsRuntimeException() {
            ValidationException ex = new ValidationException("Error");
            assertTrue(ex instanceof RuntimeException);
        }

        @Test
        @DisplayName("ValidationException - 可以被拋出和捕獲")
        void validationException_CanBeThrown() {
            assertThrows(ValidationException.class, () -> {
                throw new ValidationException("Test");
            });
        }

        @Test
        @DisplayName("ValidationException - null 欄位處理")
        void validationException_NullField() {
            ValidationException ex = new ValidationException("Error", null);
            assertNull(ex.getField());
        }

        @Test
        @DisplayName("ValidationException - 空訊息")
        void validationException_EmptyMessage() {
            ValidationException ex = new ValidationException("");
            assertEquals("", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("RestaurantNotFoundException Tests")
    class RestaurantNotFoundExceptionTests {
        @Test
        @DisplayName("RestaurantNotFoundException - 單參數建構子")
        void restaurantNotFoundException_SingleArgConstructor() {
            RestaurantNotFoundException ex = new RestaurantNotFoundException("Not found");
            assertEquals("Not found", ex.getMessage());
            assertNull(ex.getRestaurantId());
        }

        @Test
        @DisplayName("RestaurantNotFoundException - 雙參數建構子")
        void restaurantNotFoundException_TwoArgConstructor() {
            RestaurantNotFoundException ex = new RestaurantNotFoundException("Not found", "rest-123");
            assertEquals("Not found", ex.getMessage());
            assertEquals("rest-123", ex.getRestaurantId());
        }

        @Test
        @DisplayName("RestaurantNotFoundException - getRestaurantId 返回餐廳 ID")
        void restaurantNotFoundException_GetRestaurantId() {
            RestaurantNotFoundException ex = new RestaurantNotFoundException("Error", "abc-456");
            assertEquals("abc-456", ex.getRestaurantId());
        }

        @Test
        @DisplayName("RestaurantNotFoundException - toString 包含訊息")
        void restaurantNotFoundException_ToString_ContainsMessage() {
            RestaurantNotFoundException ex = new RestaurantNotFoundException("Restaurant not found");
            String result = ex.toString();
            assertTrue(result.contains("Restaurant not found"));
            assertTrue(result.contains("RestaurantNotFoundException"));
        }

        @Test
        @DisplayName("RestaurantNotFoundException - toString 包含餐廳 ID")
        void restaurantNotFoundException_ToString_ContainsRestaurantId() {
            RestaurantNotFoundException ex = new RestaurantNotFoundException("Error", "rest-789");
            String result = ex.toString();
            assertTrue(result.contains("restaurantId='rest-789'"));
        }

        @Test
        @DisplayName("RestaurantNotFoundException - toString 無餐廳 ID 時不包含 ID")
        void restaurantNotFoundException_ToString_NoRestaurantId() {
            RestaurantNotFoundException ex = new RestaurantNotFoundException("Not found");
            String result = ex.toString();
            assertFalse(result.contains("restaurantId='"));
            assertTrue(result.contains("message='Not found'"));
        }

        @Test
        @DisplayName("RestaurantNotFoundException - 是 RuntimeException 子類")
        void restaurantNotFoundException_IsRuntimeException() {
            RestaurantNotFoundException ex = new RestaurantNotFoundException("Error");
            assertTrue(ex instanceof RuntimeException);
        }

        @Test
        @DisplayName("RestaurantNotFoundException - 可以被拋出和捕獲")
        void restaurantNotFoundException_CanBeThrown() {
            assertThrows(RestaurantNotFoundException.class, () -> {
                throw new RestaurantNotFoundException("Test");
            });
        }

        @Test
        @DisplayName("RestaurantNotFoundException - null 餐廳 ID 處理")
        void restaurantNotFoundException_NullRestaurantId() {
            RestaurantNotFoundException ex = new RestaurantNotFoundException("Error", null);
            assertNull(ex.getRestaurantId());
        }

        @Test
        @DisplayName("RestaurantNotFoundException - 空字串餐廳 ID")
        void restaurantNotFoundException_EmptyRestaurantId() {
            RestaurantNotFoundException ex = new RestaurantNotFoundException("Error", "");
            assertEquals("", ex.getRestaurantId());
        }

        @Test
        @DisplayName("RestaurantNotFoundException - 空訊息")
        void restaurantNotFoundException_EmptyMessage() {
            RestaurantNotFoundException ex = new RestaurantNotFoundException("");
            assertEquals("", ex.getMessage());
        }
    }
}
