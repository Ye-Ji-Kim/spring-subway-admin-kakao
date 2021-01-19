package subway.line;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import subway.station.StationResponse;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class LineController {
    private LineDao lineDao;
    private SectionService sectionService;
    private LineService lineService;
    private SectionDao sectionDao;

    public LineController(LineDao lineDao, SectionService sectionService, LineService lineService, SectionDao sectionDao) {
        this.lineDao = lineDao;
        this.sectionService = sectionService;
        this.lineService = lineService;
        this.sectionDao = sectionDao;
    }

    @PostMapping("/lines")
    public ResponseEntity<LineResponse> createStation(@RequestBody LineRequest lineRequest) {
        Line line = new Line(lineRequest.getName(), lineRequest.getColor());
        Line newLine = lineDao.save(line);
        if (newLine == null) {
            return ResponseEntity.badRequest().build();
        }
        sectionDao.insert(new Section(newLine.getId(), lineRequest.getUpStationId(), lineRequest.getDownStationId(), lineRequest.getDistance()));
        LineResponse lineResponse = new LineResponse(newLine.getId(), newLine.getName(), newLine.getColor());
        return ResponseEntity.created(URI.create("/lines/" + newLine.getId())).body(lineResponse);
    }

    @GetMapping(value = "/lines", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LineResponse>> showLines() {
        List<LineResponse> responses = lineDao.findAll().stream()
                .map(line -> new LineResponse(line.getId(), line.getName(), line.getColor()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/lines/{lineId}")
    public ResponseEntity<LineResponse> showLine(@PathVariable Long lineId) {
        Line line = lineDao.findLineById(lineId);
        if (line == null) {
            return ResponseEntity.badRequest().build();
        }
        List<StationResponse> stationResponses = lineService.getStations(line).stream()
                .map(station -> new StationResponse(station.getId(), station.getName()))
                .collect(Collectors.toList());

        LineResponse lineResponse = new LineResponse(line.getId(), line.getName(), line.getColor(), stationResponses);
        return ResponseEntity.ok(lineResponse);
    }

    @PutMapping("/lines/{id}")
    public ResponseEntity<LineResponse> updateLine(@PathVariable Long id, @RequestBody LineRequest lineRequest) {
        Line line = new Line(lineRequest.getName(), lineRequest.getColor());
        lineDao.updateById(id, line);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/lines/{id}")
    public ResponseEntity deleteStation(@PathVariable Long id) {
        lineDao.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/lines/{lineId}/sections")
    public ResponseEntity<LineResponse> createSection(@PathVariable Long lineId, @RequestBody SectionRequest sectionRequest) {
        Section newSection = new Section(lineId, sectionRequest.getUpStationId(), sectionRequest.getDownStationId(), sectionRequest.getDistance());
        try {
            sectionService.insert(newSection);
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/lines/{lineId}/sections")
    public ResponseEntity<LineResponse> deleteSection(@PathVariable Long lineId, @RequestParam Long stationId) {
        try {
            sectionService.delete(lineId, stationId);
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
    }

}
