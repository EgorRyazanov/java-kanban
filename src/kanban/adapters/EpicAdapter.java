package kanban.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import kanban.model.Epic;
import kanban.model.TaskStatus;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EpicAdapter extends TypeAdapter<Epic> {
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public void write(JsonWriter jsonWriter, Epic epic) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("id").value(epic.getId());
        jsonWriter.name("title").value(epic.getTitle());
        jsonWriter.name("description").value(epic.getDescription());
        jsonWriter.name("status").value(epic.getStatus().toString());
        jsonWriter.name("type").value(epic.getType().toString());
        jsonWriter.name("duration").value(epic.getDuration().toMinutes());

        if (epic.getStartTime() != null) {
            jsonWriter.name("startTime").value(epic.getStartTime().format(timeFormatter));
        } else {
            jsonWriter.name("startTime").nullValue();
        }
        jsonWriter.name("subtasksIds");
        jsonWriter.beginArray();
        for (Integer id : epic.getSubtasksIds()) {
            jsonWriter.value(id);
        }
        jsonWriter.endArray();
        jsonWriter.name("endDate");
        if (epic.getEndDate() != null) {
            jsonWriter.value(epic.getEndDate().format(timeFormatter));
        } else {
            jsonWriter.nullValue();
        }
        jsonWriter.endObject();
    }

    @Override
    public Epic read(JsonReader jsonReader) throws IOException {
        int id = 0;
        String title = "";
        String description = "";
        TaskStatus status = TaskStatus.NEW;
        Duration duration = Duration.ZERO;
        LocalDateTime startTime = null;
        LocalDateTime endDate = null;
        List<Integer> subtasksIds = new ArrayList<>();

        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String fieldName = jsonReader.nextName();
            switch (fieldName) {
                case "id":
                    id = jsonReader.nextInt();
                    break;
                case "title":
                    title = jsonReader.nextString();
                    break;
                case "description":
                    description = jsonReader.nextString();
                    break;
                case "status":
                    status = TaskStatus.valueOf(jsonReader.nextString());
                    break;
                case "type":
                    jsonReader.nextString();
                    break;
                case "duration":
                    duration = Duration.ofMinutes(jsonReader.nextLong());
                    break;
                case "startTime":
                    if (jsonReader.peek() == JsonToken.NULL) {
                        jsonReader.nextNull();
                        startTime = null;
                    } else {
                        startTime = LocalDateTime.parse(jsonReader.nextString(), timeFormatter);
                    }
                    break;
                case "subtasksIds":
                    jsonReader.beginArray();
                    while (jsonReader.hasNext()) {
                        subtasksIds.add(jsonReader.nextInt());
                    }
                    jsonReader.endArray();
                    break;
                case "endDate":
                    if (jsonReader.peek() == JsonToken.NULL) {
                        jsonReader.nextNull();
                        endDate = null;
                    } else {
                        endDate = LocalDateTime.parse(jsonReader.nextString(), timeFormatter);
                    }
                    break;
                default:
                    jsonReader.skipValue();
                    break;
            }
        }
        jsonReader.endObject();

        Epic epic = new Epic(title, description, id, duration, startTime);
        epic.setStatus(status);
        epic.setEndDate(endDate);

        for (Integer subtaskId : subtasksIds) {
            epic.addSubtask(subtaskId);
        }

        return epic;
    }
}