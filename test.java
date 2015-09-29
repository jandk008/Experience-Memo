package com.ebay.cbt.arvato.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.eaby.cbt.arvato.pojo.PayData;
import com.eaby.cbt.arvato.pojo.ServiceResponse;
import com.eaby.cbt.arvato.pojo.UserData;
import com.ebay.cbt.arvato.util.StringUtil;
import com.ebay.cbt.sf.pojo.Promotion;
import com.ebay.cbt.sf.service.ServiceExecutor;

public class TestServiceExecutor {
	private static ServiceExecutor executor;
	private static ServiceResponse response;
	private static final String CHARSET_ISSUE_NAMEPREFIX = "å²èå¤«åå»ºçè½¬ç ä¸­æå¥å±";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		executor = ServiceExecutor.getInstance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateCoupon() {
		// pay data
		PayData payData = new PayData();
		payData.init();
		payData.buildTestData(2);
		
		// user data 
		List<UserData> userDatas = new ArrayList<UserData>();
		UserData userData1 = new UserData();
		userData1.initTestData(1, 2);
		UserData userData2 = new UserData();
		userData2.initTestData(2, 2);
		userDatas.add(userData1);
		userDatas.add(userData2);
		
//		response = executor.createCoupon(payData, userDatas);
		response = ArvatoServiceClient.createCoupon(payData, userDatas);
		System.out.println(response.getReason());
		assertArrayEquals(response.getReason(), new int[]{1}, new int[]{response.getStatus()});
	}

	@Test
	public void testUpdateCoupon() {
		// pay data
		PayData payData = new PayData();
		payData.init();
		payData.setCoupon_code("http://www.ebay.cn/jd_cardv3_test/events/consent/d3k%253D");
		payData.setCoupon_title("test0830");
//		payData.setCoupon_type(1);
		
//		response = executor.updateCoupon(payData, null);
		System.out.println(response.getReason());
		assertArrayEquals(response.getReason(), new int[] { 1 },
				new int[] { response.getStatus() });
	}

	@Test
	public void testQueryCoupon() {
		// pay data
		PayData payData = new PayData();
		payData.init();
		payData.setCoupon_title("æµè¯12123");
		
//		response = executor.queryCoupon(payData);
		response = ArvatoServiceClient.queryCoupon(payData);
		System.out.println(response.getReason());
		assertArrayEquals(response.getReason(), new int[] { 1 },
				new int[] { response.getStatus() });
	}
	
	@Test
	public void testSplitUrl(){
		PayData payData = new PayData();
		payData.setCoupon_code("http://www.ebay.cn/jd_cardv3_test/events/consent/d3g%253D");
		assertEquals("check coupon code ", "d3g%253D", payData.getCoupon_code());
	}
	
	@Test 
	public void testCharsetOfTitle(){
		// pay data
				PayData payData = new PayData();
				payData.init();
				payData.buildTestData(2);
				String title = getCampaignTitle("701O0000000R4m7IAC");
				if (title.equals("")){
					return;
				}
				payData.setCoupon_title(title + System.currentTimeMillis());
				
				// user data 
				List<UserData> userDatas = new ArrayList<UserData>();
				UserData userData1 = new UserData();
				userData1.initTestData(1, 2);
				UserData userData2 = new UserData();
				userData2.initTestData(2, 2);
				userDatas.add(userData1);
				userDatas.add(userData2);
				
//				response = executor.createCoupon(payData, userDatas);
				response = ArvatoServiceClient.createCoupon(payData, userDatas);
				System.out.println(response.getReason());
				assertArrayEquals(response.getReason(), new int[]{1}, new int[]{response.getStatus()});
	}
	@Test
	public void testEncodeConvert() throws Exception{
		// iso 18XXX-1
//		String title = getCampaignTitle("701O0000000R4Z2IAK");
//		String title = getCampaignTitle("701O0000000R5hCIAS");
		String title = "ji";
		System.out.println(title);
//		String test = StringUtil.charsetChanger(title, StringUtil.CHARSET_UTF8, StringUtil.CHARSET_ISO8859_1);
//		System.out.println(test);
//		String encoded = DES.getInstance().encrypt(title, false, false);
//		System.out.println(encoded);
//		String decode = DES.getInstance().decrypt(encoded, false, false);
//		System.out.println(decode);
//		String after = StringUtil.charsetChanger(decode, StringUtil.CHARSET_ISO8859_1, StringUtil.CHARSET_UTF8);
		String after = new String (title.getBytes("UTF-8"), StringUtil.CHARSET_ISO8859_1);
		after = new String(after.getBytes(), StringUtil.CHARSET_UTF8);
		System.out.println(after);
		if (com.ebay.app.raptor.promocommon.util.StringUtil.isUTF8(title)){
			System.out.println("this is based on UTF-8");
		}else {
			System.out.println("This is not based on UTF-8");
		}
//		System.out.println(Charset.defaultCharset().name());
		// TODO Auto-generated catch block
	}
	
	public String getCampaignTitle(String campaignID){
		String lastModifiedDate = "2015-09-27T06:09:43.000Z";
		try{
//			executor.invalidateCurrentSession();
			List<Promotion> promotions = executor.queryPromotions(lastModifiedDate);
			ServiceExecutor.displayFieldsInList(promotions);
			for (Promotion promotion : promotions) {
				if (promotion.getId().equals(campaignID)){
					return promotion.getTitle();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";
	}
	
}
