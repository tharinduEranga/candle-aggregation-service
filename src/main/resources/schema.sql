CREATE TABLE IF NOT EXISTS candles (
    symbol         VARCHAR(32)     NOT NULL,
    interval_code  VARCHAR(8)      NOT NULL,
    bucket_time    BIGINT          NOT NULL,
    open           NUMERIC(38, 18) NOT NULL,
    high           NUMERIC(38, 18) NOT NULL,
    low            NUMERIC(38, 18) NOT NULL,
    close          NUMERIC(38, 18) NOT NULL,
    volume         BIGINT          NOT NULL,
    PRIMARY KEY (symbol, interval_code, bucket_time)
);
