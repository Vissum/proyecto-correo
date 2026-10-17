--
-- PostgreSQL database dump
--

\restrict DMw2Ewav2kn6GJbINvYmfaK2IqSN5ZlOWTYPVis3Ud6HEYNgipzCshdD4baeAkP

-- Dumped from database version 18.6
-- Dumped by pg_dump version 18.6

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
-- Name: citext; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS citext WITH SCHEMA public;


--
-- Name: EXTENSION citext; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION citext IS 'data type for case-insensitive character strings';


--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: adjuntos; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.adjuntos (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    mensaje_id uuid NOT NULL,
    nombre_archivo character varying(255) NOT NULL,
    tipo_mime character varying(100) NOT NULL,
    tamano_bytes bigint NOT NULL,
    ruta_servidor character varying(500) NOT NULL,
    fecha_subida timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT adjuntos_tamano_bytes_check CHECK ((tamano_bytes > 0))
);


ALTER TABLE public.adjuntos OWNER TO postgres;

--
-- Name: carpetas; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.carpetas (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    usuario_id uuid NOT NULL,
    nombre character varying(50) NOT NULL,
    es_sistema boolean DEFAULT false,
    fecha_creacion timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.carpetas OWNER TO postgres;

--
-- Name: enrutamiento_buzones; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.enrutamiento_buzones (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    mensaje_id uuid NOT NULL,
    destinatario_id uuid NOT NULL,
    carpeta_id uuid NOT NULL,
    tipo_envio character varying(20) NOT NULL,
    leido boolean DEFAULT false,
    destacado boolean DEFAULT false,
    fecha_recepcion timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT enrutamiento_buzones_tipo_envio_check CHECK (((tipo_envio)::text = ANY ((ARRAY['para'::character varying, 'cc'::character varying, 'cco'::character varying, 'remitente'::character varying, 'enviado'::character varying, 'borrador'::character varying, 'PARA'::character varying, 'CC'::character varying, 'CCO'::character varying, 'ENVIADO'::character varying, 'BORRADOR'::character varying])::text[])))
);


ALTER TABLE public.enrutamiento_buzones OWNER TO postgres;

--
-- Name: mensajes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.mensajes (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    remitente_id uuid NOT NULL,
    asunto character varying(255) DEFAULT ''::character varying NOT NULL,
    cuerpo_plano text,
    cuerpo_html text,
    pais_origen character varying(100) NOT NULL,
    ip_origen inet,
    fecha_envio timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    busqueda tsvector GENERATED ALWAYS AS (to_tsvector('spanish'::regconfig, (((COALESCE(asunto, ''::character varying))::text || ' '::text) || COALESCE(cuerpo_plano, ''::text)))) STORED,
    borrador_para text,
    borrador_cc text,
    borrador_cco text,
    CONSTRAINT mensajes_check CHECK (((cuerpo_plano IS NOT NULL) OR (cuerpo_html IS NOT NULL)))
);


ALTER TABLE public.mensajes OWNER TO postgres;

--
-- Name: tokens_recuperacion; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tokens_recuperacion (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    usuario_id uuid NOT NULL,
    token_hash character varying(64) NOT NULL,
    expira_en timestamp with time zone NOT NULL,
    usado boolean DEFAULT false NOT NULL,
    fecha_creacion timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.tokens_recuperacion OWNER TO postgres;

--
-- Name: usuarios; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.usuarios (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    correo character varying(255) NOT NULL,
    contrasena_hash character varying(255) NOT NULL,
    nombre_completo character varying(150) NOT NULL,
    estado character varying(20) DEFAULT 'activo'::character varying,
    cuota_almacenamiento_mb integer DEFAULT 1024,
    fecha_creacion timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    es_administrador boolean DEFAULT false NOT NULL,
    CONSTRAINT usuarios_cuota_almacenamiento_mb_check CHECK ((cuota_almacenamiento_mb > 0)),
    CONSTRAINT usuarios_estado_check CHECK (((estado)::text = ANY ((ARRAY['activo'::character varying, 'suspendido'::character varying, 'eliminado'::character varying])::text[])))
);


ALTER TABLE public.usuarios OWNER TO postgres;

--
-- Data for Name: adjuntos; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.adjuntos (id, mensaje_id, nombre_archivo, tipo_mime, tamano_bytes, ruta_servidor, fecha_subida) FROM stdin;
fe0efc88-fff6-4d24-ab73-8f9e5e869392	8ae2b908-fab8-4fb2-b9cb-65a43a398edd	Proyecto Final.pptx.pdf	application/pdf	2328921	C:\\correo_adjuntos\\fb7363d7-666a-4378-bee8-3a8f07c66d08	2026-10-13 20:13:42.16448-06
1b76b7b9-8bc4-4f74-b970-2deed0f3420c	28d36cc4-57ae-4a83-99a8-ab2c656626af	Proyecto Final.pptx.pdf	application/pdf	2328921	C:\\correo_adjuntos\\0e085a7c-2bbe-4c3e-861e-681401711e52	2026-10-13 20:30:36.836963-06
01e34e19-fbb3-4907-a83f-1006d6f2b481	f0196633-1919-4936-8144-0605c721d712	Proyecto Final.pptx.pdf	application/pdf	2328921	C:\\correo_adjuntos\\ed918891-b149-4ccb-b7e6-73c159673b32	2026-10-13 20:45:13.769289-06
75f10459-183f-42bf-9152-b1caba931d27	20afbc69-f9b9-446f-bfea-9e4af8b2788b	practica 4.docx	application/vnd.openxmlformats-officedocument.wordprocessingml.document	1450212	C:\\correo_adjuntos\\e8d076c3-bf4b-402e-a7dd-bcf5771af7de	2026-10-13 20:47:13.126891-06
\.


--
-- Data for Name: carpetas; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.carpetas (id, usuario_id, nombre, es_sistema, fecha_creacion) FROM stdin;
247b2d8b-b48f-4b98-87ea-17d5e909c3e6	6da83050-bf61-42e9-aeb0-43ac9e8e0638	Recibidos	t	2026-10-12 20:25:27.982746-06
1647ee78-f519-44d1-9215-6f2d74093135	6da83050-bf61-42e9-aeb0-43ac9e8e0638	Enviados	t	2026-10-12 22:03:14.551803-06
f6069d7b-e58a-4d84-acd2-4811a0a64a5f	6da83050-bf61-42e9-aeb0-43ac9e8e0638	Entrada	t	2026-10-12 22:04:35.311639-06
386c0230-ffab-444a-b849-cffbc1d9befa	befd2695-9b76-4c44-a33a-dc3a1427075f	Entrada	t	2026-10-12 22:07:41.186416-06
dabfda28-3fc0-4dee-bc1c-9123047ce6c3	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	Entrada	t	2026-10-12 23:17:24.874412-06
3f544971-c27f-41bb-b2bd-c902d5d5e568	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	Enviados	t	2026-10-12 23:17:24.880329-06
55e347a6-fb73-4384-aaa6-92ee9b616e80	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	Borradores	t	2026-10-12 23:17:24.883017-06
a946029a-b87c-4079-8d8f-c68aa10bc2bb	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	Spam	t	2026-10-12 23:17:24.885248-06
4c1c2895-8f92-4a8f-b265-44ec028974d4	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	Papelera	t	2026-10-12 23:17:24.887646-06
ba7f2e39-d233-4f9b-b710-73a4c020bde6	befd2695-9b76-4c44-a33a-dc3a1427075f	Borradores	t	2026-10-13 00:09:41.895973-06
0387025b-c04a-4220-a111-cb8d6e043ef5	6da83050-bf61-42e9-aeb0-43ac9e8e0638	Borradores	t	2026-10-13 00:09:41.895973-06
124db6ed-1902-4e59-a0a4-c3d2ffee6e2a	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	Archivo	t	2026-10-14 00:11:28.081639-06
3ec4cd44-f785-4acf-b0b4-ac7e3dca2a5f	befd2695-9b76-4c44-a33a-dc3a1427075f	Archivo	t	2026-10-14 00:11:28.081639-06
caf248e2-6505-4de0-97ed-2b1ab233a53c	6da83050-bf61-42e9-aeb0-43ac9e8e0638	Archivo	t	2026-10-14 00:11:28.081639-06
\.


--
-- Data for Name: enrutamiento_buzones; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.enrutamiento_buzones (id, mensaje_id, destinatario_id, carpeta_id, tipo_envio, leido, destacado, fecha_recepcion) FROM stdin;
a6041392-0e69-484b-8ecd-ce8f5da59746	80583e9b-a8d6-4d85-997c-942ba5d9c7c4	6da83050-bf61-42e9-aeb0-43ac9e8e0638	1647ee78-f519-44d1-9215-6f2d74093135	remitente	t	f	2026-10-12 22:07:58.890122-06
644cfe92-c25b-4b4d-aa9b-6db2b3debc18	80583e9b-a8d6-4d85-997c-942ba5d9c7c4	befd2695-9b76-4c44-a33a-dc3a1427075f	386c0230-ffab-444a-b849-cffbc1d9befa	para	t	f	2026-10-12 22:07:58.893677-06
310c1478-7bbd-45e5-8b46-7e77be3fb564	fe47358a-649c-4535-a4c3-8069df92f919	6da83050-bf61-42e9-aeb0-43ac9e8e0638	1647ee78-f519-44d1-9215-6f2d74093135	remitente	t	f	2026-10-12 23:29:26.850052-06
3b481217-cec0-45b3-a29f-9786d614c06a	1071c57b-dc89-4095-9ed7-cb1359ffe115	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	3f544971-c27f-41bb-b2bd-c902d5d5e568	remitente	t	f	2026-10-12 23:35:16.455063-06
a8a2a6ab-59dc-4092-8da7-2981ddebf89b	1071c57b-dc89-4095-9ed7-cb1359ffe115	6da83050-bf61-42e9-aeb0-43ac9e8e0638	f6069d7b-e58a-4d84-acd2-4811a0a64a5f	para	t	f	2026-10-12 23:35:16.460573-06
8fc03319-e9f6-4d9c-afe7-e136282ba895	02bae405-c85f-444a-bcc1-c0423a45f263	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	3f544971-c27f-41bb-b2bd-c902d5d5e568	remitente	t	f	2026-10-13 00:04:53.691765-06
1c5a948b-f2a6-4032-8ffb-cd22a3fd72b1	02bae405-c85f-444a-bcc1-c0423a45f263	befd2695-9b76-4c44-a33a-dc3a1427075f	386c0230-ffab-444a-b849-cffbc1d9befa	cc	f	f	2026-10-13 00:04:53.699121-06
df7da5da-344c-4346-83b8-5dab1b0dad57	02bae405-c85f-444a-bcc1-c0423a45f263	6da83050-bf61-42e9-aeb0-43ac9e8e0638	f6069d7b-e58a-4d84-acd2-4811a0a64a5f	para	t	f	2026-10-13 00:04:53.695338-06
38f469a7-9acc-44a1-bb6d-ee171d533250	f0196633-1919-4936-8144-0605c721d712	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	3f544971-c27f-41bb-b2bd-c902d5d5e568	remitente	t	f	2026-10-13 20:45:13.649389-06
6aa781f8-fdcc-4c19-b1db-f0bd90be03ba	f0196633-1919-4936-8144-0605c721d712	6da83050-bf61-42e9-aeb0-43ac9e8e0638	f6069d7b-e58a-4d84-acd2-4811a0a64a5f	para	t	f	2026-10-13 20:45:13.653177-06
0b203022-4857-49aa-b083-35ee8a0e5ff6	53a4eda4-bc4a-4021-bf88-7237fe5d4ed6	6da83050-bf61-42e9-aeb0-43ac9e8e0638	f6069d7b-e58a-4d84-acd2-4811a0a64a5f	para	t	f	2026-10-13 19:16:19.809984-06
2f983c13-8a4b-400a-baa1-da8a36fc1430	20afbc69-f9b9-446f-bfea-9e4af8b2788b	6da83050-bf61-42e9-aeb0-43ac9e8e0638	f6069d7b-e58a-4d84-acd2-4811a0a64a5f	para	t	f	2026-10-13 20:47:13.046925-06
923513b3-2a9d-45aa-ae3a-ae206c9627aa	53a4eda4-bc4a-4021-bf88-7237fe5d4ed6	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	3f544971-c27f-41bb-b2bd-c902d5d5e568	remitente	t	f	2026-10-13 19:16:19.805229-06
3f66e56b-4bac-49fb-8b90-33e487a6ac68	20afbc69-f9b9-446f-bfea-9e4af8b2788b	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	3f544971-c27f-41bb-b2bd-c902d5d5e568	remitente	t	f	2026-10-13 20:47:13.043105-06
16f0badf-b501-4e5a-a869-09d785c973f3	28d36cc4-57ae-4a83-99a8-ab2c656626af	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	55e347a6-fb73-4384-aaa6-92ee9b616e80	borrador	t	f	2026-10-13 20:26:56.724046-06
ee4fa431-ca30-439a-a4ef-1246aa594c29	455d36a9-02c5-4e15-8b83-4c711b9b7031	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	55e347a6-fb73-4384-aaa6-92ee9b616e80	borrador	t	f	2026-10-13 20:11:47.603257-06
3944ede0-5767-475b-9e13-e2610ef03cd7	8ae2b908-fab8-4fb2-b9cb-65a43a398edd	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	3f544971-c27f-41bb-b2bd-c902d5d5e568	remitente	t	f	2026-10-13 20:13:42.031495-06
69b0fdfe-8ee0-4385-806f-f0e242af8546	8ae2b908-fab8-4fb2-b9cb-65a43a398edd	6da83050-bf61-42e9-aeb0-43ac9e8e0638	f6069d7b-e58a-4d84-acd2-4811a0a64a5f	para	t	f	2026-10-13 20:13:42.036712-06
5fec4608-8bef-48b6-9d07-d72a6ee8d2d2	8089973f-d5f8-433b-bef4-bee78e8d6294	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	55e347a6-fb73-4384-aaa6-92ee9b616e80	borrador	t	f	2026-10-13 00:19:00.759319-06
d01fa82f-9534-42fd-a5d7-b1b36db2e970	1b32fb60-fbec-45c8-bcdd-32e4576f2fd3	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	55e347a6-fb73-4384-aaa6-92ee9b616e80	borrador	t	f	2026-10-13 20:22:39.61515-06
f98c1583-8368-4ed5-9513-df9b50b5c16c	f0196633-1919-4936-8144-0605c721d712	befd2695-9b76-4c44-a33a-dc3a1427075f	386c0230-ffab-444a-b849-cffbc1d9befa	cc	f	f	2026-10-13 20:45:13.658391-06
bd14bc67-d266-4963-af6f-a6b9180651e8	fe47358a-649c-4535-a4c3-8069df92f919	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	dabfda28-3fc0-4dee-bc1c-9123047ce6c3	para	t	t	2026-10-12 23:29:26.855887-06
be406a6c-8392-43df-b598-167fe1cf8ff3	e2b37cf3-c752-4896-bed2-7f69e797e3af	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	55e347a6-fb73-4384-aaa6-92ee9b616e80	borrador	t	f	2026-10-14 00:36:43.78064-06
\.


--
-- Data for Name: mensajes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.mensajes (id, remitente_id, asunto, cuerpo_plano, cuerpo_html, pais_origen, ip_origen, fecha_envio, borrador_para, borrador_cc, borrador_cco) FROM stdin;
80583e9b-a8d6-4d85-997c-942ba5d9c7c4	6da83050-bf61-42e9-aeb0-43ac9e8e0638	Prueba	Hola, este es un mensaje de prueba	\N	Guatemala	\N	2026-10-12 22:07:58.886541-06	\N	\N	\N
fe47358a-649c-4535-a4c3-8069df92f919	6da83050-bf61-42e9-aeb0-43ac9e8e0638	prueba 2	hola	\N	Guatemala	\N	2026-10-12 23:29:26.836438-06	\N	\N	\N
1071c57b-dc89-4095-9ed7-cb1359ffe115	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	Tienes un mensaje	soy la sombra\r\n	\N	Guatemala	\N	2026-10-12 23:35:16.450641-06	\N	\N	\N
02bae405-c85f-444a-bcc1-c0423a45f263	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	prueba 3 cc	1234	\N	Guatemala	\N	2026-10-13 00:04:53.677133-06	\N	\N	\N
8089973f-d5f8-433b-bef4-bee78e8d6294	2fb02dd0-8924-4251-8a46-ae2751bd9ae1			\N	Guatemala	\N	2026-10-13 00:19:00.746319-06	\N	\N	\N
9b5ad16e-e448-46e4-945a-74f8a0fa5693	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	borrador	borrador	\N	Guatemala	\N	2026-10-13 19:15:37.253051-06	\N	\N	\N
53a4eda4-bc4a-4021-bf88-7237fe5d4ed6	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	borrador	borrador	\N	Guatemala	\N	2026-10-13 19:16:19.799349-06	\N	\N	\N
455d36a9-02c5-4e15-8b83-4c711b9b7031	2fb02dd0-8924-4251-8a46-ae2751bd9ae1			\N	Guatemala	\N	2026-10-13 20:11:47.592052-06	\N	\N	\N
e0fc58d3-b2b7-4671-a9c2-3276f2ba29c1	2fb02dd0-8924-4251-8a46-ae2751bd9ae1			\N	Guatemala	\N	2026-10-13 20:12:00.57661-06	\N	\N	\N
8ae2b908-fab8-4fb2-b9cb-65a43a398edd	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	prueba con archivo	hola xd	\N	Guatemala	\N	2026-10-13 20:13:42.025863-06	\N	\N	\N
1b32fb60-fbec-45c8-bcdd-32e4576f2fd3	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	prueba borrador todo	prueba	\N	Guatemala	\N	2026-10-13 20:22:39.60757-06	\N	\N	\N
28d36cc4-57ae-4a83-99a8-ab2c656626af	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	prueba borrador todo	prueba	\N	Guatemala	\N	2026-10-13 20:26:56.717927-06	ken@correo		
f0196633-1919-4936-8144-0605c721d712	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	prueba limite de bytes	prueba limite de bytes	\N	Guatemala	\N	2026-10-13 20:45:13.634755-06	\N	\N	\N
20afbc69-f9b9-446f-bfea-9e4af8b2788b	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	Prueba 2 limte de bytes		\N	Guatemala	\N	2026-10-13 20:47:13.038045-06	\N	\N	\N
e2b37cf3-c752-4896-bed2-7f69e797e3af	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	2	2	\N	Guatemala	\N	2026-10-14 00:36:43.770267-06	ken@correo.com		
\.


--
-- Data for Name: tokens_recuperacion; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.tokens_recuperacion (id, usuario_id, token_hash, expira_en, usado, fecha_creacion) FROM stdin;
254bde83-ace7-4f62-97d4-4e476ab756af	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	3771b5ac8a0ab7a53066b59e21d0f114077865297841a9f8f431563bc6c2a30d	2026-10-14 00:17:53.212283-06	t	2026-10-13 23:47:53.213266-06
61c1bdfe-0649-4468-94f1-3c44cd9f5976	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	b9e4edb090c589a3728f9bddbb93588ebc58cf6c3a4fb5acc93d6fddacb821b4	2026-10-14 00:22:23.10291-06	t	2026-10-13 23:52:23.104484-06
d9e5c12d-e390-43e9-9bea-8708dabf7233	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	4a79550031df212bc9716e6eefe8131401b4e99072e340555af0eb045e3a7298	2026-10-14 00:22:25.061716-06	t	2026-10-13 23:52:25.063614-06
b0a1437f-50e2-49a9-be0a-b8af2cf8dff3	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	59be9888d858312b538b70e465a3ffe6ee293d62ba4367f35d5a3e0feb83de06	2026-10-14 00:22:27.74918-06	t	2026-10-13 23:52:27.751168-06
5192b4fa-90a8-4cc8-ad14-106a9a9d9ac8	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	e7dbc13d1f4517a37703955cead12ac10a68eec39ce8eb9bf420abce20114713	2026-10-14 00:22:27.774098-06	t	2026-10-13 23:52:27.774991-06
cbc7c555-5727-496b-91e6-a2b1b7fa81de	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	f3a278a251fa7e0f6188c8adeab2a0204b07e94ef9d4d5739278b4f69cb66e1b	2026-10-14 00:24:24.8832-06	t	2026-10-13 23:54:24.88459-06
c34fc8af-4bd3-4339-8642-cc5845bf47bc	2fb02dd0-8924-4251-8a46-ae2751bd9ae1	5f60e0e88eda0f5eec4bf41b582175e78f37b4458d746b216d486e25eaffacdf	2026-10-14 00:50:21.678552-06	f	2026-10-14 00:20:21.680139-06
\.


--
-- Data for Name: usuarios; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.usuarios (id, correo, contrasena_hash, nombre_completo, estado, cuota_almacenamiento_mb, fecha_creacion, es_administrador) FROM stdin;
befd2695-9b76-4c44-a33a-dc3a1427075f	destino@correo.com	PRUEBA	Usuario Destino	activo	1024	2026-10-12 22:07:16.891133-06	f
2fb02dd0-8924-4251-8a46-ae2751bd9ae1	keno@correo.com	pbkdf2$210000$OnAerWo/eO5OrWC/1TQjTg==$x7ioLw5yg+oTkMkuhtQUKHjNX0e7YLUyGTmz5POIeHM=	Kenito	activo	1024	2026-10-12 23:17:24.864789-06	f
6da83050-bf61-42e9-aeb0-43ac9e8e0638	ken@correo.com	pbkdf2$210000$32Qkz4nJDqyyBDXj+BW1Hg==$xKyckaGJhizv1U3hQEOGRSPlPYjiiCM8BcsH/jvJHHo=	Ken Prueba	activo	1024	2026-10-12 20:25:27.982746-06	t
\.


--
-- Name: adjuntos adjuntos_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.adjuntos
    ADD CONSTRAINT adjuntos_pkey PRIMARY KEY (id);


--
-- Name: carpetas carpetas_id_usuario_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.carpetas
    ADD CONSTRAINT carpetas_id_usuario_id_key UNIQUE (id, usuario_id);


--
-- Name: carpetas carpetas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.carpetas
    ADD CONSTRAINT carpetas_pkey PRIMARY KEY (id);


--
-- Name: carpetas carpetas_usuario_id_nombre_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.carpetas
    ADD CONSTRAINT carpetas_usuario_id_nombre_key UNIQUE (usuario_id, nombre);


--
-- Name: enrutamiento_buzones enrutamiento_buzones_mensaje_id_destinatario_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.enrutamiento_buzones
    ADD CONSTRAINT enrutamiento_buzones_mensaje_id_destinatario_id_key UNIQUE (mensaje_id, destinatario_id);


--
-- Name: enrutamiento_buzones enrutamiento_buzones_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.enrutamiento_buzones
    ADD CONSTRAINT enrutamiento_buzones_pkey PRIMARY KEY (id);


--
-- Name: mensajes mensajes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mensajes
    ADD CONSTRAINT mensajes_pkey PRIMARY KEY (id);


--
-- Name: tokens_recuperacion tokens_recuperacion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tokens_recuperacion
    ADD CONSTRAINT tokens_recuperacion_pkey PRIMARY KEY (id);


--
-- Name: tokens_recuperacion tokens_recuperacion_token_hash_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tokens_recuperacion
    ADD CONSTRAINT tokens_recuperacion_token_hash_key UNIQUE (token_hash);


--
-- Name: usuarios usuarios_correo_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_correo_key UNIQUE (correo);


--
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);


--
-- Name: idx_adjuntos_mensaje; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_adjuntos_mensaje ON public.adjuntos USING btree (mensaje_id);


--
-- Name: idx_carpetas_usuario; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_carpetas_usuario ON public.carpetas USING btree (usuario_id);


--
-- Name: idx_enrutamiento_busqueda; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_enrutamiento_busqueda ON public.enrutamiento_buzones USING btree (destinatario_id, carpeta_id, leido);


--
-- Name: idx_enrutamiento_mensaje; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_enrutamiento_mensaje ON public.enrutamiento_buzones USING btree (mensaje_id);


--
-- Name: idx_mensajes_busqueda; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_mensajes_busqueda ON public.mensajes USING gin (busqueda);


--
-- Name: idx_mensajes_remitente; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_mensajes_remitente ON public.mensajes USING btree (remitente_id);


--
-- Name: idx_tokens_recuperacion_usuario; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_tokens_recuperacion_usuario ON public.tokens_recuperacion USING btree (usuario_id);


--
-- Name: adjuntos adjuntos_mensaje_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.adjuntos
    ADD CONSTRAINT adjuntos_mensaje_id_fkey FOREIGN KEY (mensaje_id) REFERENCES public.mensajes(id) ON DELETE CASCADE;


--
-- Name: carpetas carpetas_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.carpetas
    ADD CONSTRAINT carpetas_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;


--
-- Name: enrutamiento_buzones enrutamiento_buzones_carpeta_id_destinatario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.enrutamiento_buzones
    ADD CONSTRAINT enrutamiento_buzones_carpeta_id_destinatario_id_fkey FOREIGN KEY (carpeta_id, destinatario_id) REFERENCES public.carpetas(id, usuario_id) ON DELETE CASCADE;


--
-- Name: enrutamiento_buzones enrutamiento_buzones_destinatario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.enrutamiento_buzones
    ADD CONSTRAINT enrutamiento_buzones_destinatario_id_fkey FOREIGN KEY (destinatario_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;


--
-- Name: enrutamiento_buzones enrutamiento_buzones_mensaje_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.enrutamiento_buzones
    ADD CONSTRAINT enrutamiento_buzones_mensaje_id_fkey FOREIGN KEY (mensaje_id) REFERENCES public.mensajes(id) ON DELETE CASCADE;


--
-- Name: mensajes mensajes_remitente_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mensajes
    ADD CONSTRAINT mensajes_remitente_id_fkey FOREIGN KEY (remitente_id) REFERENCES public.usuarios(id) ON DELETE RESTRICT;


--
-- Name: tokens_recuperacion tokens_recuperacion_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tokens_recuperacion
    ADD CONSTRAINT tokens_recuperacion_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict DMw2Ewav2kn6GJbINvYmfaK2IqSN5ZlOWTYPVis3Ud6HEYNgipzCshdD4baeAkP

