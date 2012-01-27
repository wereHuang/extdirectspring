/**
 * Copyright 2010-2011 Ralph Schaer <ralphschaer@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.ralscha.extdirectspring.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StringUtils;

import ch.ralscha.extdirectspring.bean.ExtDirectRequest;

/**
 * Tests for {@link JsonHandler}.
 * 
 * @author Ralph Schaer
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/testApplicationContext.xml")
public class JsonHandlerTest {

	@Autowired
	private JsonHandler jsonHandler;

	@Test
	public void testserializeObject() {
		assertEquals("null", jsonHandler.writeValueAsString(null));
		assertEquals("\"a\"", jsonHandler.writeValueAsString("a"));
		assertEquals("1", jsonHandler.writeValueAsString(1));
		assertEquals("true", jsonHandler.writeValueAsString(true));

		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("one", 1);
		map.put("two", "2");
		map.put("three", null);
		map.put("four", false);
		map.put("five", new int[] { 1, 2 });

		String expected = "{\"one\":1,\"two\":\"2\",\"three\":null,\"four\":false,\"five\":[1,2]}";
		assertEquals(expected, jsonHandler.writeValueAsString(map));

		JsonTestBean testBean = new JsonTestBean(1, "2", null, false, new Integer[] { 1, 2 });
		expected = "{\"a\":1,\"b\":\"2\",\"c\":null,\"d\":false,\"e\":[1,2]}";
		assertEquals(expected, jsonHandler.writeValueAsString(testBean));

	}

	@Test
	public void testserializeObjectBoolean() {
		assertEquals("null", jsonHandler.writeValueAsString(null, true));
		assertEquals("\"a\"", jsonHandler.writeValueAsString("a", true));
		assertEquals("1", jsonHandler.writeValueAsString(1, true));
		assertEquals("true", jsonHandler.writeValueAsString(true, true));

		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("one", 1);
		map.put("two", "2");
		map.put("three", null);
		map.put("four", false);
		map.put("five", new int[] { 1, 2 });

		String expected = "{\n  \"one\" : 1,\n  \"two\" : \"2\",\n  \"three\" : null,\n  \"four\" : false,\n  \"five\" : [ 1, 2 ]\n}";
		assertEquals(expected, jsonHandler.writeValueAsString(map, true).replace("\r", ""));

		JsonTestBean testBean = new JsonTestBean(1, "2", null, false, new Integer[] { 1, 2 });
		expected = "{\n  \"a\" : 1,\n  \"b\" : \"2\",\n  \"c\" : null,\n  \"d\" : false,\n  \"e\" : [ 1, 2 ]\n}";
		assertEquals(expected, jsonHandler.writeValueAsString(testBean, true).replace("\r", ""));

	}

	@Test
	public void testdeserializeStringTypeReferenceOfT() {
		String json = "[\"1\",\"2\",\"3\",\"4\"]";
		List<String> result = jsonHandler.readValue(json, new TypeReference<List<String>>() {/* empty */
		});
		assertEquals(4, result.size());
		assertEquals("1", result.get(0));
		assertEquals("2", result.get(1));
		assertEquals("3", result.get(2));
		assertEquals("4", result.get(3));

		Object o = jsonHandler.readValue("", new TypeReference<String>() {/* empty */
		});
		assertNull(o);

		o = jsonHandler.readValue("xy", new TypeReference<Integer>() {/* empty */
		});
		assertNull(o);

	}

	@Test
	public void testdeserializeStringClassOfT() {
		assertNull(jsonHandler.readValue("null", String.class));
		assertEquals("a", jsonHandler.readValue("\"a\"", String.class));
		assertEquals(Integer.valueOf(1), jsonHandler.readValue("1", Integer.class));
		assertTrue(jsonHandler.readValue("true", Boolean.class));

		String json1 = "{\"a\":1,\"b\":\"2\",\"c\":null,\"d\":false,\"e\":[1,2]}";
		String json2 = "{\r\n  \"a\" : 1,\r\n  \"b\" : \"2\",\r\n  \"c\" : null,\r\n  \"d\" : false,\r\n  \"e\" : [ 1, 2 ]\r\n}";
		JsonTestBean testBean = jsonHandler.readValue(json1, JsonTestBean.class);
		assertEquals(Integer.valueOf(1), testBean.getA());
		assertEquals("2", testBean.getB());
		assertNull(testBean.getC());
		assertFalse(testBean.getD());
		assertArrayEquals(new Integer[] { 1, 2 }, testBean.getE());

		testBean = jsonHandler.readValue(json2, JsonTestBean.class);
		assertEquals(Integer.valueOf(1), testBean.getA());
		assertEquals("2", testBean.getB());
		assertNull(testBean.getC());
		assertFalse(testBean.getD());
		assertArrayEquals(new Integer[] { 1, 2 }, testBean.getE());

		Object o = jsonHandler.readValue("", String.class);
		assertNull(o);

		o = jsonHandler.readValue("xy", Integer.class);
		assertNull(o);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testJsonUtilObject() {
		ExtDirectRequest req = new ExtDirectRequest();
		req.setAction("testAction");
		req.setMethod("testMethod");
		req.setTid(1);
		req.setType("testType");
		req.setData(new Object[] { "one", "two" });

		String json = jsonHandler.writeValueAsString(req);
		assertNotNull(json);
		assertTrue(StringUtils.hasText(json));

		ExtDirectRequest desReq = jsonHandler.readValue(json, ExtDirectRequest.class);
		assertNotNull(desReq);

		assertEquals(req.getAction(), desReq.getAction());
		assertArrayEquals((Object[]) req.getData(), ((List<Object>) desReq.getData()).toArray());
		assertEquals(req.getMethod(), desReq.getMethod());
		assertEquals(req.getTid(), desReq.getTid());
		assertEquals(req.getType(), desReq.getType());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testJsonList() throws JsonGenerationException, JsonMappingException, IOException {
		List<ExtDirectRequest> requests = new ArrayList<ExtDirectRequest>();

		ExtDirectRequest req = new ExtDirectRequest();
		req.setAction("testAction1");
		req.setMethod("testMethod1");
		req.setTid(1);
		req.setType("testType1");
		req.setData(new Object[] { "one" });
		requests.add(req);

		req = new ExtDirectRequest();
		req.setAction("testAction2");
		req.setMethod("testMethod2");
		req.setTid(2);
		req.setType("testType2");
		req.setData(new Object[] { "two" });
		requests.add(req);

		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(requests);

		List<ExtDirectRequest> desRequests = jsonHandler.readValue(json, new TypeReference<List<ExtDirectRequest>>() {/* empty */
		});

		assertEquals(requests.size(), desRequests.size());
		for (int i = 0; i < requests.size(); i++) {
			req = requests.get(i);
			ExtDirectRequest desReq = desRequests.get(i);

			assertEquals(req.getAction(), desReq.getAction());
			assertArrayEquals((Object[]) req.getData(), ((List<Object>) desReq.getData()).toArray());
			assertEquals(req.getMethod(), desReq.getMethod());
			assertEquals(req.getTid(), desReq.getTid());
			assertEquals(req.getType(), desReq.getType());
		}
	}

}