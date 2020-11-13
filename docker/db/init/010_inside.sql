--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.15
-- Dumped by pg_dump version 11.5

-- Started on 2019-09-05 09:49:16

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;


SET default_tablespace = '';

SET default_with_oids = false;



--
-- TOC entry 203 (class 1259 OID 524796)
-- Name: weather; Type: TABLE; Schema: public; Owner: weather_buffer_user
--

CREATE TABLE public.weather_inside (
    id_weather_inside bigint NOT NULL,
    temperature double precision,
    humidity double precision,
    "time" timestamp with time zone
);


ALTER TABLE public.weather_inside OWNER TO weather_buffer_user;



CREATE SEQUENCE public.weather_inside_id_weather_inside_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.weather_inside_id_weather_inside_seq OWNER TO weather_buffer_user;


ALTER SEQUENCE public.weather_inside_id_weather_inside_seq OWNED BY public.weather_inside.id_weather_inside;



ALTER TABLE ONLY public.weather_inside ALTER COLUMN id_weather_inside SET DEFAULT nextval('public.weather_inside_id_weather_inside_seq'::regclass);


ALTER TABLE ONLY public.weather_inside
    ADD CONSTRAINT weather_inside_pkey PRIMARY KEY (id_weather_inside);


CREATE INDEX ind_weather_inside_temperature ON public.weather_inside USING btree (temperature);

CREATE INDEX ind_weather_inside_time ON public.weather_inside USING btree ("time");


GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE public.weather_inside TO weather_buffer_user;


GRANT SELECT,USAGE ON SEQUENCE public.weather_inside_id_weather_inside_seq TO weather_buffer_user;


-- Completed on 2019-09-05 09:49:18

--
-- weather_buffer_userQL database dump complete
--

