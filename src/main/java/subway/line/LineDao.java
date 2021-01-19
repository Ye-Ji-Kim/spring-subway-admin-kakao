package subway.line;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Locale;

@Repository
public class LineDao {
    private JdbcTemplate jdbcTemplate;

    public LineDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Line> lineRowMapper = (resultSet, rowNum) -> {
        Line line = new Line(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("color"),
                resultSet.getLong("up_station_id"),
        resultSet.getLong("down_station_id")
        );
        return line;
    };

    public Line save(Line line) {
        if (countByName(line.getName()) > 0) {
            throw new IllegalArgumentException("이미 존재하는 노선입니다.");
        }
        String sql = "insert into line (name, color, up_station_id, down_station_id) values (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, line.getName());
            ps.setString(2, line.getColor());
            ps.setLong(3, line.getUpStationId());
            ps.setLong(4, line.getDownStationId());
            return ps;
        }, keyHolder);

        return new Line(keyHolder.getKey().longValue(), line.getName(), line.getColor());
    }

    public List<Line> findAll() {
        String sql = "select id, name, color from line";
        return jdbcTemplate.query(sql, lineRowMapper);
    }

    public Line findLineById(Long id) {
        String sql = "select id, name, color from line where id = ?";
        return jdbcTemplate.queryForObject(sql, lineRowMapper, id);
    }

    public List<Line> findLineByName(String name) {
        String sql = "select id, name, color from line where name = ?";
        return jdbcTemplate.query(sql, lineRowMapper, name);
    }

    public int countByName(String name) {
        String sql = "select count(*) from line where name = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, name);
    }

    public void updateById(Long id, Line line) {
        String sql = "update line set name = ?, color = ? where id = ?";
        jdbcTemplate.update(sql, line.getName(), line.getColor(), id);
    }

    public void deleteById(Long id) {
        String sql = "delete from line where id = ?";
        jdbcTemplate.update(sql, Long.valueOf(id));
    }


}
