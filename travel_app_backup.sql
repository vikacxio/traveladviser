--
-- PostgreSQL database dump
--

\restrict uQgBEQevKxRTDlb2aELD6EBGw0cdwiv3Tu5NVN5KJV52HUVoOAdWxW9jugAs71f

-- Dumped from database version 17.9 (Homebrew)
-- Dumped by pg_dump version 17.9 (Homebrew)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: postgis; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry and geography spatial types and functions';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: categories; Type: TABLE; Schema: public; Owner: vikacxio
--

CREATE TABLE public.categories (
    id integer NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.categories OWNER TO vikacxio;

--
-- Name: categories_id_seq; Type: SEQUENCE; Schema: public; Owner: vikacxio
--

CREATE SEQUENCE public.categories_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.categories_id_seq OWNER TO vikacxio;

--
-- Name: categories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vikacxio
--

ALTER SEQUENCE public.categories_id_seq OWNED BY public.categories.id;


--
-- Name: cities; Type: TABLE; Schema: public; Owner: vikacxio
--

CREATE TABLE public.cities (
    id integer NOT NULL,
    name character varying(255),
    state character varying(255),
    country character varying(255),
    latitude double precision,
    longitude double precision,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.cities OWNER TO vikacxio;

--
-- Name: cities_id_seq; Type: SEQUENCE; Schema: public; Owner: vikacxio
--

CREATE SEQUENCE public.cities_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.cities_id_seq OWNER TO vikacxio;

--
-- Name: cities_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vikacxio
--

ALTER SEQUENCE public.cities_id_seq OWNED BY public.cities.id;


--
-- Name: place_tags; Type: TABLE; Schema: public; Owner: vikacxio
--

CREATE TABLE public.place_tags (
    place_id bigint NOT NULL,
    tag_id integer NOT NULL
);


ALTER TABLE public.place_tags OWNER TO vikacxio;

--
-- Name: places; Type: TABLE; Schema: public; Owner: vikacxio
--

CREATE TABLE public.places (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    category_id integer,
    latitude double precision NOT NULL,
    longitude double precision NOT NULL,
    avg_rating double precision DEFAULT 0,
    total_ratings integer DEFAULT 0,
    price_level integer,
    best_season character varying(255),
    city character varying(255),
    state character varying(255),
    country character varying(255),
    source character varying(255),
    source_id character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    location public.geometry(Point,4326),
    CONSTRAINT places_best_season_check CHECK (((best_season)::text = ANY (ARRAY['SUMMER'::text, 'WINTER'::text, 'MONSOON'::text, 'ALL'::text]))),
    CONSTRAINT places_price_level_check CHECK (((price_level >= 1) AND (price_level <= 5)))
);


ALTER TABLE public.places OWNER TO vikacxio;

--
-- Name: places_id_seq; Type: SEQUENCE; Schema: public; Owner: vikacxio
--

CREATE SEQUENCE public.places_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.places_id_seq OWNER TO vikacxio;

--
-- Name: places_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vikacxio
--

ALTER SEQUENCE public.places_id_seq OWNED BY public.places.id;


--
-- Name: tags; Type: TABLE; Schema: public; Owner: vikacxio
--

CREATE TABLE public.tags (
    id integer NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.tags OWNER TO vikacxio;

--
-- Name: tags_id_seq; Type: SEQUENCE; Schema: public; Owner: vikacxio
--

CREATE SEQUENCE public.tags_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tags_id_seq OWNER TO vikacxio;

--
-- Name: tags_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: vikacxio
--

ALTER SEQUENCE public.tags_id_seq OWNED BY public.tags.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: vikacxio
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    email character varying(255),
    name character varying(255),
    password character varying(255),
    provider character varying(255)
);


ALTER TABLE public.users OWNER TO vikacxio;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: vikacxio
--

ALTER TABLE public.users ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: categories id; Type: DEFAULT; Schema: public; Owner: vikacxio
--

ALTER TABLE ONLY public.categories ALTER COLUMN id SET DEFAULT nextval('public.categories_id_seq'::regclass);


--
-- Name: cities id; Type: DEFAULT; Schema: public; Owner: vikacxio
--

ALTER TABLE ONLY public.cities ALTER COLUMN id SET DEFAULT nextval('public.cities_id_seq'::regclass);


--
-- Name: places id; Type: DEFAULT; Schema: public; Owner: vikacxio
--

ALTER TABLE ONLY public.places ALTER COLUMN id SET DEFAULT nextval('public.places_id_seq'::regclass);


--
-- Name: tags id; Type: DEFAULT; Schema: public; Owner: vikacxio
--

ALTER TABLE ONLY public.tags ALTER COLUMN id SET DEFAULT nextval('public.tags_id_seq'::regclass);


--
-- Data for Name: categories; Type: TABLE DATA; Schema: public; Owner: vikacxio
--

COPY public.categories (id, name) FROM stdin;
1	Mountain
2	Beach
3	Temple
4	Museum
5	Park
6	Fort
7	Lake
8	Waterfall
9	Adventure
10	Historical
11	Cultural
12	Wildlife
13	Religious
\.


--
-- Data for Name: cities; Type: TABLE DATA; Schema: public; Owner: vikacxio
--

COPY public.cities (id, name, state, country, latitude, longitude, created_at) FROM stdin;
1	Manali	Himachal Pradesh	India	32.2396	77.1887	2026-04-25 17:57:15.784061
2	Goa	Goa	India	15.2993	73.8243	2026-04-25 17:57:15.784061
3	Varanasi	Uttar Pradesh	India	25.3548	82.9855	2026-04-25 17:57:15.784061
4	Agra	Uttar Pradesh	India	27.1767	78.0081	2026-04-25 17:57:15.784061
5	Jaipur	Rajasthan	India	26.8967	75.825	2026-04-25 17:57:15.784061
6	Delhi	Delhi	India	28.7041	77.1025	2026-04-25 17:57:15.784061
7	Bangalore	Karnataka	India	12.9716	77.5946	2026-04-25 17:57:15.784061
8	Chennai	Tamil Nadu	India	13.0827	80.2707	2026-04-25 17:57:15.784061
9	Mumbai	Maharashtra	India	19.076	72.8777	2026-04-25 17:57:15.784061
10	Kolkata	West Bengal	India	22.5726	88.3639	2026-04-25 17:57:15.784061
\.


--
-- Data for Name: place_tags; Type: TABLE DATA; Schema: public; Owner: vikacxio
--

COPY public.place_tags (place_id, tag_id) FROM stdin;
1	1
1	2
1	3
1	4
1	5
2	2
2	5
2	6
2	7
2	8
3	8
3	9
3	10
3	11
25	5
25	10
25	11
25	12
25	13
17	4
17	11
17	14
17	15
\.


--
-- Data for Name: places; Type: TABLE DATA; Schema: public; Owner: vikacxio
--

COPY public.places (id, name, description, category_id, latitude, longitude, avg_rating, total_ratings, price_level, best_season, city, state, country, source, source_id, created_at, location) FROM stdin;
1	Manali	Scenic mountain town	1	32.2396	77.1887	4.6	890	2	SUMMER	Manali	Himachal Pradesh	India	manual	manali_001	2026-04-25 17:57:15.784486	\N
2	Goa Beaches	Popular beach destination	2	15.2993	73.8243	4.5	1200	2	WINTER	Goa	Goa	India	manual	goa_001	2026-04-25 17:57:15.784486	\N
3	Varanasi Temple	Holy city on Ganges	3	25.3548	82.9855	4.4	560	1	ALL	Varanasi	Uttar Pradesh	India	manual	varanasi_001	2026-04-25 17:57:15.784486	\N
4	Himalayan Trekking	Adventure trekking	9	32.2396	77.1887	4.7	450	2	SUMMER	Manali	Himachal Pradesh	India	manual	himalayan_trek_001	2026-04-25 17:57:15.784486	\N
5	Spiti Valley	Remote mountain valley	1	31.85	78.15	4.8	340	1	SUMMER	Kaza	Himachal Pradesh	India	manual	spiti_001	2026-04-25 17:57:15.784486	\N
6	Khajjiar	Mini Switzerland	1	32.5817	76.6733	4.5	280	2	SUMMER	Khajjiar	Himachal Pradesh	India	manual	khajjiar_001	2026-04-25 17:57:15.784486	\N
7	Auli	Skiing destination	1	30.0333	79.6167	4.6	220	3	WINTER	Chopta	Uttarakhand	India	manual	auli_001	2026-04-25 17:57:15.784486	\N
8	Pangong Lake Ladakh	Stunning saltwater lake	7	33.7837	78.5497	4.7	420	3	SUMMER	Leh	Ladakh	India	manual	pangong_001	2026-04-25 17:57:15.784486	\N
9	Vembanad Lake Kerala	Largest backwater lake	7	9.68	76.36	4.4	200	2	ALL	Kochi	Kerala	India	manual	vembanad_001	2026-04-25 17:57:15.784486	\N
10	Niagara Falls India	Jog Falls Karnataka	8	14.8428	75.354	4.5	180	1	MONSOON	Jog Falls	Karnataka	India	manual	jog_falls_001	2026-04-25 17:57:15.784486	\N
11	Dudhsagar Falls	Four tier waterfall Goa	8	15.3047	73.9897	4.4	200	2	MONSOON	Goa	Goa	India	manual	dudhsagar_001	2026-04-25 17:57:15.784486	\N
12	Athirapally Falls Kerala	Queen of waterfalls	8	10.2706	76.5705	4.3	160	1	MONSOON	Thrissur	Kerala	India	manual	athirapally_001	2026-04-25 17:57:15.784486	\N
13	Kunchikal Falls Karnataka	Highest waterfall	8	14.3928	75.4358	4.2	140	1	MONSOON	Agumbe	Karnataka	India	manual	kunchikal_001	2026-04-25 17:57:15.784486	\N
14	Triveni Falls	Confluence of three rivers	8	30.4159	78.8529	4.1	110	1	MONSOON	Chopta	Uttarakhand	India	manual	triveni_001	2026-04-25 17:57:15.784486	\N
15	Gokak Falls Karnataka	Circular horseshoe falls	8	15.9006	74.9878	4	100	1	MONSOON	Gokak	Karnataka	India	manual	gokak_001	2026-04-25 17:57:15.784486	\N
16	Kapildhara Falls Madhya Pradesh	Narmada river falls	8	22.4756	77.8994	3.9	90	1	MONSOON	Indore	Madhya Pradesh	India	manual	kapildhara_001	2026-04-25 17:57:15.784486	\N
17	Ranthambore National Park	Tiger reserve	12	26.0122	76.5028	4.6	310	3	WINTER	Sawai Madhopur	Rajasthan	India	manual	ranthambore_np_001	2026-04-25 17:57:15.784486	\N
18	Kaziranga National Park	One horned rhino	12	26.5824	93.217	4.5	240	2	WINTER	Golaghat	Assam	India	manual	kaziranga_001	2026-04-25 17:57:15.784486	\N
19	Kanha National Park	Tiger and baison	12	22.55	80.6	4.4	200	2	WINTER	Mandla	Madhya Pradesh	India	manual	kanha_001	2026-04-25 17:57:15.784486	\N
20	Bandhavgarh National Park	White tiger reserve	12	23.8833	81.5667	4.3	180	2	WINTER	Umaria	Madhya Pradesh	India	manual	bandhavgarh_001	2026-04-25 17:57:15.784486	\N
21	Sundarbans National Park	Bengal tiger and mangroves	12	21.9497	89.1833	4.4	220	2	WINTER	Sundarban	West Bengal	India	manual	sundarbans_001	2026-04-25 17:57:15.784486	\N
22	Periyar National Park Kerala	Elephant and tiger reserve	12	9.55	77.25	4.3	190	2	ALL	Thekkady	Kerala	India	manual	periyar_001	2026-04-25 17:57:15.784486	\N
23	Pench National Park	Tiger and leopard	12	21.7933	78.85	4.2	160	2	WINTER	Seoni	Madhya Pradesh	India	manual	pench_001	2026-04-25 17:57:15.784486	\N
24	Gir National Park Gujarat	Asiatic lion reserve	12	21.1667	70.6667	4.2	150	2	WINTER	Junagadh	Gujarat	India	manual	gir_001	2026-04-25 17:57:15.784486	\N
25	Taj Mahal	Monument to love	10	27.1751	78.0421	4.7	890	2	ALL	Agra	Uttar Pradesh	India	manual	taj_mahal_001	2026-04-25 17:57:15.784486	\N
26	Ajanta Caves	Buddhist cave paintings	10	19.8874	75.7733	4.4	190	1	ALL	Aurangabad	Maharashtra	India	manual	ajanta_001	2026-04-25 17:57:15.784486	\N
27	Ellora Caves	Hindu Buddhist Islamic caves	10	19.9009	75.4818	4.5	210	1	ALL	Aurangabad	Maharashtra	India	manual	ellora_001	2026-04-25 17:57:15.784486	\N
28	Fatehpur Sikri	Ghost city Akbar	10	27.0885	77.8711	4.3	180	2	ALL	Agra	Uttar Pradesh	India	manual	fatehpur_001	2026-04-25 17:57:15.784486	\N
29	Sanchi Stupa	Buddhist monument	10	23.4833	77.7833	4.2	140	1	ALL	Bhopal	Madhya Pradesh	India	manual	sanchi_001	2026-04-25 17:57:15.784486	\N
30	Hampi Ruins	Vijayanagara empire	10	15.335	76.4625	4.5	260	1	ALL	Hospet	Karnataka	India	manual	hampi_001	2026-04-25 17:57:15.784486	\N
31	Konark Sun Temple	Chariot temple Odisha	10	19.888	86.0901	4.3	170	1	ALL	Puri	Odisha	India	manual	konark_001	2026-04-25 17:57:15.784486	\N
32	Mahabalipuram Temples	Shore temples	10	12.5656	80.1925	4.4	200	1	ALL	Mahabalipuram	Tamil Nadu	India	manual	mahabalipuram_001	2026-04-25 17:57:15.784486	\N
33	Virupaksha Temple Hampi	Temple in ruins	10	15.3275	76.4625	4.5	190	1	ALL	Hospet	Karnataka	India	manual	virupaksha_001	2026-04-25 17:57:15.784486	\N
34	Brihadisvara Temple Thanjavur	Chola architecture	10	10.787	79.1378	4.4	180	1	ALL	Thanjavur	Tamil Nadu	India	manual	brihadisvara_001	2026-04-25 17:57:15.784486	\N
\.


--
-- Data for Name: spatial_ref_sys; Type: TABLE DATA; Schema: public; Owner: vikacxio
--

COPY public.spatial_ref_sys (srid, auth_name, auth_srid, srtext, proj4text) FROM stdin;
\.


--
-- Data for Name: tags; Type: TABLE DATA; Schema: public; Owner: vikacxio
--

COPY public.tags (id, name) FROM stdin;
1	cool_place
2	outdoor
3	hiking
4	adventure
5	family_friendly
6	water_spot
7	beach
8	budget_friendly
9	spiritual
10	historical
11	photography
12	romantic
13	luxury
14	wildlife
15	nature_lover
16	offbeat
17	trekking
18	camping
19	scenic
20	backpacking
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: vikacxio
--

COPY public.users (id, email, name, password, provider) FROM stdin;
1	test@example.com	Test User	$2a$10$vrnYBztiN2W4gUsZuh49uesZ0.5NjBROXhLPEyaZJepUONXQ7qLX6	LOCAL
2	john@example.com	John Doe	$2a$10$Iq5F7PoDq5VhSWP1TwuBVenbaTnJSNpE4yrZnOKBluLbwb9P4MtdS	LOCAL
3	john1@example.com	John Doe	$2a$10$jw2LfRUd8UDaBitCvYAdz.fhzUHTFuPzlUgm837KE.5F6zRptedcS	LOCAL
4	john2@example.com	John Doe	$2a$10$SUvaVV7Jl2ZjWnMXgdAbuOB5KBxdOInnsXoPW3zjwyuwy5tU3z3tW	LOCAL
5	john123@example.com	John Doe	$2a$10$Gvc0lwWDPBPJpc4bqR4i5ezFyrWEgVPxUULL5yDrGZDOZRpLnv0oq	LOCAL
6	john12345@example.com	John Doe	$2a$10$UhtdBckv7bIj4vt00vGDSuQXeeYO4.1wjb49RoFl6LxFG.b5H7y/W	LOCAL
7	john123451@example.com	John Doe	$2a$10$UJU0EA8JZ8hgc/Bb5tMuMuBUVI6DMDfYChFrzSTI1bSOiLJKIPtlO	LOCAL
8	john123456@example.com	John Doe	$2a$10$SR0FO.YzJNDavQ3f3jdz.uByuk6/NhaXXqnTvFcWeIkge78jPAt6a	LOCAL
\.


--
-- Name: categories_id_seq; Type: SEQUENCE SET; Schema: public; Owner: vikacxio
--

SELECT pg_catalog.setval('public.categories_id_seq', 13, true);


--
-- Name: cities_id_seq; Type: SEQUENCE SET; Schema: public; Owner: vikacxio
--

SELECT pg_catalog.setval('public.cities_id_seq', 10, true);


--
-- Name: places_id_seq; Type: SEQUENCE SET; Schema: public; Owner: vikacxio
--

SELECT pg_catalog.setval('public.places_id_seq', 34, true);


--
-- Name: tags_id_seq; Type: SEQUENCE SET; Schema: public; Owner: vikacxio
--

SELECT pg_catalog.setval('public.tags_id_seq', 20, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: vikacxio
--

SELECT pg_catalog.setval('public.users_id_seq', 8, true);


--
-- Name: categories categories_name_key; Type: CONSTRAINT; Schema: public; Owner: vikacxio
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_name_key UNIQUE (name);


--
-- Name: categories categories_pkey; Type: CONSTRAINT; Schema: public; Owner: vikacxio
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_pkey PRIMARY KEY (id);


--
-- Name: cities cities_pkey; Type: CONSTRAINT; Schema: public; Owner: vikacxio
--

ALTER TABLE ONLY public.cities
    ADD CONSTRAINT cities_pkey PRIMARY KEY (id);


--
-- Name: place_tags place_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: vikacxio
--

ALTER TABLE ONLY public.place_tags
    ADD CONSTRAINT place_tags_pkey PRIMARY KEY (place_id, tag_id);


--
-- Name: places places_pkey; Type: CONSTRAINT; Schema: public; Owner: vikacxio
--

ALTER TABLE ONLY public.places
    ADD CONSTRAINT places_pkey PRIMARY KEY (id);


--
-- Name: tags tags_name_key; Type: CONSTRAINT; Schema: public; Owner: vikacxio
--

ALTER TABLE ONLY public.tags
    ADD CONSTRAINT tags_name_key UNIQUE (name);


--
-- Name: tags tags_pkey; Type: CONSTRAINT; Schema: public; Owner: vikacxio
--

ALTER TABLE ONLY public.tags
    ADD CONSTRAINT tags_pkey PRIMARY KEY (id);


--
-- Name: places unique_source; Type: CONSTRAINT; Schema: public; Owner: vikacxio
--

ALTER TABLE ONLY public.places
    ADD CONSTRAINT unique_source UNIQUE (source, source_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: vikacxio
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: place_tags place_tags_place_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vikacxio
--

ALTER TABLE ONLY public.place_tags
    ADD CONSTRAINT place_tags_place_id_fkey FOREIGN KEY (place_id) REFERENCES public.places(id) ON DELETE CASCADE;


--
-- Name: place_tags place_tags_tag_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vikacxio
--

ALTER TABLE ONLY public.place_tags
    ADD CONSTRAINT place_tags_tag_id_fkey FOREIGN KEY (tag_id) REFERENCES public.tags(id) ON DELETE CASCADE;


--
-- Name: places places_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: vikacxio
--

ALTER TABLE ONLY public.places
    ADD CONSTRAINT places_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.categories(id) ON DELETE SET NULL;


--
-- PostgreSQL database dump complete
--

\unrestrict uQgBEQevKxRTDlb2aELD6EBGw0cdwiv3Tu5NVN5KJV52HUVoOAdWxW9jugAs71f

