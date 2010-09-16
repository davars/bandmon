--
-- PostgreSQL database dump
--

-- Started on 2010-08-13 11:56:22

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 1501 (class 1259 OID 87791)
-- Dependencies: 3
-- Name: exceptions; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE exceptions (
    id integer NOT NULL,
    thrown_at timestamp with time zone NOT NULL,
    ex_type text NOT NULL,
    message text,
    stack_trace text
);


--
-- TOC entry 1500 (class 1259 OID 87789)
-- Dependencies: 3 1501
-- Name: exceptions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE exceptions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1791 (class 0 OID 0)
-- Dependencies: 1500
-- Name: exceptions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE exceptions_id_seq OWNED BY exceptions.id;


--
-- TOC entry 1499 (class 1259 OID 87769)
-- Dependencies: 3
-- Name: test_results; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE test_results (
    id integer NOT NULL,
    host_ip text,
    start_time timestamp with time zone NOT NULL,
    end_time timestamp with time zone NOT NULL,
    upstream_lo double precision,
    upstream_hi double precision,
    downstream_lo double precision,
    downstream_hi double precision,
    mlab_ip text,
    ping_sent integer,
    ping_rcvd integer,
    ping_min double precision,
    ping_avg double precision,
    ping_max double precision,
    ping_mdev double precision
);


--
-- TOC entry 1502 (class 1259 OID 87861)
-- Dependencies: 1589 3
-- Name: summary; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW summary AS
    SELECT test_results.host_ip, max(test_results.start_time) AS last_run, avg(test_results.upstream_lo) AS upstream_lo, avg(test_results.downstream_lo) AS downstream_lo, avg(test_results.downstream_hi) AS downstream_hi, avg(test_results.ping_avg) AS latency, ((100.0 * (sum(test_results.ping_rcvd))::numeric) / (sum(test_results.ping_sent))::numeric) AS received_pct FROM test_results GROUP BY test_results.host_ip;


--
-- TOC entry 1498 (class 1259 OID 87767)
-- Dependencies: 3 1499
-- Name: test_results_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE test_results_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1792 (class 0 OID 0)
-- Dependencies: 1498
-- Name: test_results_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE test_results_id_seq OWNED BY test_results.id;


--
-- TOC entry 1782 (class 2604 OID 87794)
-- Dependencies: 1500 1501 1501
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE exceptions ALTER COLUMN id SET DEFAULT nextval('exceptions_id_seq'::regclass);


--
-- TOC entry 1781 (class 2604 OID 87772)
-- Dependencies: 1499 1498 1499
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE test_results ALTER COLUMN id SET DEFAULT nextval('test_results_id_seq'::regclass);


--
-- TOC entry 1786 (class 2606 OID 87799)
-- Dependencies: 1501 1501
-- Name: pk_exceptions; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY exceptions
    ADD CONSTRAINT pk_exceptions PRIMARY KEY (id);


--
-- TOC entry 1784 (class 2606 OID 87777)
-- Dependencies: 1499 1499
-- Name: pk_test_results; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY test_results
    ADD CONSTRAINT pk_test_results PRIMARY KEY (id);


--
-- TOC entry 1790 (class 0 OID 0)
-- Dependencies: 3
-- Name: public; Type: ACL; Schema: -; Owner: -
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2010-08-13 11:56:29

--
-- PostgreSQL database dump complete
--
