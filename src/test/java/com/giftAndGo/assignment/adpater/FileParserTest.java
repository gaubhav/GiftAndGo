package com.giftAndGo.assignment.adpater;

import com.giftAndGo.assignment.adapter.util.FileParser;
import com.giftAndGo.assignment.domain.exception.InvalidInputException;
import com.giftAndGo.assignment.domain.model.Entry;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.giftAndGo.assignment.fixtures.Fixtures.createEntry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileParserTest {
    private FileParser fileParser;

    @BeforeEach
    public void setup() {
        fileParser = new FileParser();
    }

    @Test
    @SneakyThrows
    public void testParseFile_SkipValidation()  {
        String invalidInput = "invalid-uuid|||||-6.2|-12.1\n";
        byte[] fileContent = invalidInput.getBytes();

        List<Entry> entries = fileParser.parseFile(fileContent, true);

        assertThat(entries).hasSize(1);
        assertThat(entries.getFirst().getUuid()).isEqualTo("invalid-uuid");
        assertThat(entries.getFirst().getId()).isEmpty();
        assertThat(entries.getFirst().getName()).isEmpty();
        assertThat(entries.getFirst().getTransport()).isEmpty();
        assertThat(entries.getFirst().getTopSpeed()).isEqualTo(-12.1);
        assertThat(entries.getFirst().getAvgSpeed()).isEqualTo(-6.2);
        assertThat(entries.getFirst().getLikes()).isEmpty();
    }

    @Test
    @SneakyThrows
    public void testParseFile_ValidInput() {
        String validInput = "18148426-89e1-11ee-b9d1-0242ac120002|1X1D14|John Smith|Likes Apricots|Rides A Bike|6.2|12.1\n" +
                "3ce2d17b-e66a-4c1e-bca3-40eb1c9222c7|2X2D24|Mike Smith|Likes Grape|Drives an SUV|35.0|95.5\n";
        byte[] fileContent = validInput.getBytes();

        List<Entry> entries = fileParser.parseFile(fileContent, false);

        assertThat(entries).hasSize(2);

        assertThat(entries).containsExactly(
                createEntry("1X1D14","Likes Apricots", 6.2, 12.1, "John Smith", "Rides A Bike").uuid("18148426-89e1-11ee-b9d1-0242ac120002").build(),
                createEntry("2X2D24","Likes Grape", 35.0, 95.5, "Mike Smith", "Drives an SUV").uuid("3ce2d17b-e66a-4c1e-bca3-40eb1c9222c7").build()
                );
    }

    @Test
    @SneakyThrows
    public void testParseFile_InvalidFields() {
        String invalidInput = "invalid-uuid|||||-1.0|invalid\n";
        byte[] fileContent = invalidInput.getBytes();

        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> fileParser.parseFile(fileContent, false));
        String message = exception.getMessage();
        assertThat(message).contains("Invalid UUID format");
        assertThat(message).contains("ID cannot be empty");
        assertThat(message).contains("Name cannot be empty");
        assertThat(message).contains("Likes cannot be empty");
        assertThat(message).contains("Transport cannot be empty");
        assertThat(message).contains("Invalid top speed");
        assertThat(message).contains("Invalid UUID format");
        assertThat(message).contains("Invalid average speed");
        assertThat(message).contains("Validation failed for line: invalid-uuid|||||-1.0|invalid");
    }

    @Test
    @SneakyThrows
    public void testParseFile_MissingFields() {
        String invalidInput = "18148426-89e1-11ee-b9d1-0242ac120002|1X1D14|John Smith|Likes Apricots|Rides A Bike|6.2\n";
        byte[] fileContent = invalidInput.getBytes();

        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> fileParser.parseFile(fileContent, false));
        assertThat(exception.getMessage()).isEqualTo("Invalid file format: Expected 7 parts, got 6");
    }

    @Test
    @SneakyThrows
    public void testParseFile_EmptyFile()  {
        byte[] fileContent = new byte[0];

        List<Entry> entries = fileParser.parseFile(fileContent, false);

        assertThat(entries).isEmpty();
    }
}

