package com.giftAndGo.assignment.domain.service;

import com.giftAndGo.assignment.adapter.util.FileParser;
import com.giftAndGo.assignment.configuration.AppConfig;
import com.giftAndGo.assignment.domain.exception.InputProcessingException;
import com.giftAndGo.assignment.domain.model.Entry;
import com.giftAndGo.assignment.domain.model.Outcome;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.giftAndGo.assignment.fixtures.Fixtures.createEntry;
import static com.giftAndGo.assignment.fixtures.Fixtures.createOutcome;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileProcessingServiceTest {

    @Mock
    private FileParser fileParser;

    @Mock
    private AppConfig.FeatureProperties featureProperties;

    @Mock
    private AppConfig appConfig;

    @InjectMocks
    private FileProcessingService fileProcessingService;

    @Test
    @SneakyThrows
    public void testProcessFile_Success(){
        MultipartFile file = Mockito.mock(MultipartFile.class);
        Entry entry1 = createEntry("1","Likes apples", 10, 12.1, "John Smith", "Rides A Bike")
                .build();
        Entry entry2 = createEntry("2","Likes apricots", 60, 95.5, "Mike Smith", "Rides A Car")
                .build();
        List<Entry> entries = List.of(entry1,entry2);

        when(appConfig.getFeature()).thenReturn(featureProperties);
        when(featureProperties.isSkipValidation()).thenReturn(false);

        when(fileParser.parseFile(any(), anyBoolean())).thenReturn(entries);

        List<Outcome> outcomes = fileProcessingService.processFile(file);

        assertThat(outcomes).hasSize(2);
        assertThat(outcomes).containsExactly(
                createOutcome(12.1, "John Smith", "Rides A Bike").build(),
                createOutcome(95.5, "Mike Smith", "Rides A Car").build()
                );
    }

    @Test
    @SneakyThrows
    public void testProcessFile_ThrowsAnException(){
        MultipartFile file = Mockito.mock(MultipartFile.class);

        when(appConfig.getFeature()).thenReturn(featureProperties);
        when(featureProperties.isSkipValidation()).thenReturn(false);

        when(fileParser.parseFile(any(), anyBoolean())).thenThrow(new IOException());

        InputProcessingException exception = assertThrows(InputProcessingException.class, () -> fileProcessingService.processFile(file));
        assertThat(exception.getMessage()).isEqualTo("Unable to read the input file");
    }
}
