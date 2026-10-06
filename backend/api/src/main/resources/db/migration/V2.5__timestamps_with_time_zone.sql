-- Every timestamp becomes TIMESTAMPTZ (an absolute instant).
--
-- Before this migration, the columns below were naive TIMESTAMPs written as
-- wall-clock time in the zone of the JVM that wrote them (LocalDateTime.now(),
-- @CreationTimestamp, or a CURRENT_TIMESTAMP default evaluated in the session
-- zone, which pgjdbc aligns on the JVM zone). The history is converted by
-- reading those values in that legacy zone.
--
-- Legacy zone:
--   * `auto` (default): the TimeZone of the migrating session, i.e. the zone of
--     the API JVM running Flyway.
--   * any IANA name (e.g. `Europe/Paris`) through UPTIME_LEGACY_TIMEZONE, when
--     the API and the workers did not run in the same zone as the migrating API.
--
-- The rewrite takes an ACCESS EXCLUSIVE lock on each table: stop the workers
-- during the upgrade.
DO
$$
DECLARE
    requested   text := '${legacy_timezone}';
    legacy_zone text;
    target      record;
BEGIN
    legacy_zone := CASE
        WHEN requested = '' OR lower(requested) = 'auto' THEN current_setting('TimeZone')
        ELSE requested
    END;

    BEGIN
        PERFORM now() AT TIME ZONE legacy_zone;
    EXCEPTION
        WHEN invalid_parameter_value THEN
            RAISE EXCEPTION 'Unknown legacy time zone "%" (set UPTIME_LEGACY_TIMEZONE to an IANA name)', legacy_zone;
    END;

    RAISE NOTICE 'Converting naive timestamps to TIMESTAMPTZ, reading them in zone %', legacy_zone;

    FOR target IN
        SELECT *
        FROM (VALUES ('users', 'created_at', 'now()'),
                     ('users', 'updated_at', 'now()'),
                     ('refresh_token', 'expired_at', NULL),
                     ('refresh_token', 'created_at', 'now()'),
                     ('refresh_token', 'last_used_at', NULL),
                     ('probes', 'last_run', NULL),
                     ('probes', 'next_check_at', NULL),
                     ('probes', 'created_at', 'now()'),
                     ('probes', 'updated_at', 'now()'),
                     ('probes_monitors_logs', 'run_at', 'now()'),
                     ('notifications_channels', 'created_at', 'now()'),
                     ('notifications_channels', 'updated_at', 'now()')) AS t(table_name, column_name, column_default)
        WHERE EXISTS (SELECT 1
                      FROM information_schema.columns c
                      WHERE c.table_schema = current_schema()
                        AND c.table_name = t.table_name
                        AND c.column_name = t.column_name
                        AND c.data_type = 'timestamp without time zone')
    LOOP
        EXECUTE format('ALTER TABLE %I ALTER COLUMN %I DROP DEFAULT', target.table_name, target.column_name);
        EXECUTE format('ALTER TABLE %I ALTER COLUMN %I TYPE TIMESTAMPTZ USING %I AT TIME ZONE %L',
                       target.table_name, target.column_name, target.column_name, legacy_zone);
        IF target.column_default IS NOT NULL THEN
            EXECUTE format('ALTER TABLE %I ALTER COLUMN %I SET DEFAULT %s',
                           target.table_name, target.column_name, target.column_default);
        END IF;
    END LOOP;
END
$$;
