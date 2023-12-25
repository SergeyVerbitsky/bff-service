package com.verbitsky.converter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.verbitsky.api.converter.ServiceResponseConverter;
import com.verbitsky.api.converter.ServiceResponseConverterManager;
import com.verbitsky.api.exception.ServiceException;
import com.verbitsky.api.model.SessionModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConverterManagerImplTest {

    private static ServiceResponseConverterManager converterManager;

    @SuppressWarnings("unchecked")
    @BeforeAll
    static void setUp() {
        converterManager = new RemoteServiceResponseConverterManager(prepareConverterList());
    }

    @AfterAll
    static void tearDown() {
        converterManager = null;
    }

    @Test
    void provideConverterPositiveCase() {
        var actualConverter = converterManager.provideConverter(SessionModel.class, Object.class);
        assertNotNull(actualConverter, "Expected not null converter value");
    }

    @Test
    void provideConverterNegativeCaseServiceExceptionExpected() {
        ServiceException exception = assertThrows(
                ServiceException.class, () -> converterManager.provideConverter(SessionModel.class, String.class));

        String expectedMessage = "No suitable converter found for conversion from SessionModel to String";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void provideConverterNegativeCaseIllegalArgumentExceptionExpected() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> converterManager.provideConverter(null, String.class));

        String expectedMessage = "Received wrong method parameters (null)";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void provideConverterNegativeCaseIllegalArgumentExceptionExpected2() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> converterManager.provideConverter(SessionModel.class, null));

        String expectedMessage = "Received wrong method parameters (null)";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @SuppressWarnings("rawtypes")
    private static List prepareConverterList() {
        ServiceResponseConverter converter = mock(ServiceResponseConverter.class);
        when(converter.getResponseType()).thenReturn(SessionModel.class);
        when(converter.getResultType()).thenReturn(Object.class);
        return List.of(converter);
    }
}