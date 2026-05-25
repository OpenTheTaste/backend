package com.ott.api_user.playback.buffer;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PlaybackBatchRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String UPDATE_SQL = """
            UPDATE playback
            SET position_sec = ?,
                modified_date = NOW()
            WHERE member_id = ?
              AND contents_id = ?
              AND status = 'ACTIVE'
            """;

    @Transactional
    public void batchUpdate(Collection<PlaybackCommand> commands) {
        List<PlaybackCommand> sorted = commands.stream()
            .sorted(Comparator.comparing(PlaybackCommand::memberId)
                               .thenComparing(PlaybackCommand::contentsId))
            .toList();

        jdbcTemplate.batchUpdate(
            UPDATE_SQL,
            sorted,
            sorted.size(),
            (ps, cmd) -> {
                ps.setInt(1, cmd.positionSec());
                ps.setLong(2, cmd.memberId());
                ps.setLong(3, cmd.contentsId());
            });
    }
}
