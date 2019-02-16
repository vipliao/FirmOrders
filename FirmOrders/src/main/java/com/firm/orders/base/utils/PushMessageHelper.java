package com.firm.orders.base.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import cn.jiguang.common.ClientConfig;
import cn.jiguang.common.ServiceHelper;
import cn.jiguang.common.connection.NativeHttpClient;
import cn.jiguang.common.connection.NettyHttpClient;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jiguang.common.resp.ResponseWrapper;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.CIDResult;
import cn.jpush.api.push.GroupPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.SMS;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.audience.AudienceTarget;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.InterfaceAdapter;
import cn.jpush.api.push.model.notification.IosAlert;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import cn.jpush.api.push.model.notification.PlatformNotification;
import io.netty.handler.codec.http.HttpMethod;

@Component
public class PushMessageHelper {

	protected static final Logger LOG = LoggerFactory.getLogger(PushMessageHelper.class);

	

	public static long sendCount = 0;
	private static long sendTotalTime = 0;

	/**
	 * 创建推送客户端
	 * @param appkey 应用主键
	 * @param masterSecret 密钥
	 */
	public static void createJPushClient(String appkey,String masterSecret) {
		ClientConfig config = ClientConfig.getInstance();
		//config.setMaxRetryTimes(5);
		//config.setConnectionTimeout(10 * 1000); // 10 seconds
		//config.setSSLVersion("TLSv1.1"); // JPush server supports SSLv3, TLSv1,TLSv1.1, TLSv1.2
		//config.setApnsProduction(false); // development env
		//config.setTimeToLive(60 * 60 * 24); // one day
		// config.setGlobalPushSetting(false, 60 * 60 * 24); // development env,
		// one day
		JPushClient jPushClient = new JPushClient(masterSecret, appkey, null, config); // JPush  client
		// PushClient pushClient = new PushClient(masterSecret, appKey, null, config); // push client only
	}

	/**
	 * 使用 NettyHttpClient 异步接口发送请求
	 * 
	 * @param content
	 *            消息内容
	 * @param alias
	 *            别名
	 */
	public static void sendPushWithCallback(String appkey,String masterSecret,String content, String... alias) {
		ClientConfig clientConfig = ClientConfig.getInstance();
		String host = (String) clientConfig.get(ClientConfig.PUSH_HOST_NAME);
		final NettyHttpClient client = new NettyHttpClient(ServiceHelper.getBasicAuthorization(appkey,masterSecret),
				null, clientConfig);
		try {
			URI uri = new URI(host + clientConfig.get(ClientConfig.PUSH_PATH));
			PushPayload payload = buildPushObject_all_alias_alert(content, alias);
			client.sendRequest(HttpMethod.POST, payload.toString(), uri, new NettyHttpClient.BaseCallback() {
				@Override
				public void onSucceed(ResponseWrapper responseWrapper) {
					LOG.info("Got result: " + responseWrapper.responseContent);
				}
			});
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * 发送推送消息
	 * @param appkey 应用主键
	 * @param masterSecret 密钥
	 * @param title 消息标题
	 * @param content 消息内容
	 * @param extras 扩展信息
	 * @param alias 别名
	 */
	public static void sendPush(String appkey,String masterSecret,String title, String content, Map<String, String> extras,String... alias) throws Exception {
		ClientConfig clientConfig = ClientConfig.getInstance();
		final JPushClient jpushClient = new JPushClient(masterSecret.trim(), appkey.trim(), null, clientConfig);
		// Here you can use NativeHttpClient or NettyHttpClient or ApacheHttpClient.
		// Call setHttpClient to set httpClient,
		// If you don't invoke this method, default httpClient will use NativeHttpClient.
		// ApacheHttpClient httpClient = new ApacheHttpClient(authCode, null, clientConfig);
		// jpushClient.getPushClient().setHttpClient(httpClient);
		final PushPayload payload = buildPushObject_android_and_ios(title, content, extras,alias);
		// // For push, all you need do is to build PushPayload object.
		// PushPayload payload = buildPushObject_all_alias_alert();
		
			PushResult result = jpushClient.sendPush(payload);
			LOG.info("Got result - " + result);
			System.out.println(result);
			// 如果使用 NettyHttpClient，需要手动调用 close 方法退出进程
			// If uses NettyHttpClient, call close when finished sending
			// request, otherwise process will not exit.
			// jpushClient.close();
	}
	
	/**
	 * 给所有用户发送消息
	  * @param appkey 应用主键
	 * @param masterSecret 密钥
	 * @param title 消息标题
	 * @param content 消息内容
	 * @param extras 扩展信息
	 * @throws Exception
	 */
	public static void sendAllPush(String appkey,String masterSecret,String title,String content,Map<String, String> extras) throws Exception{
		ClientConfig clientConfig = ClientConfig.getInstance();
		final JPushClient jpushClient = new JPushClient(masterSecret.trim(), appkey.trim(), null, clientConfig);
		final PushPayload payload = buildPushObject_android_and_ios(title, content, extras);
		PushResult result = jpushClient.sendPush(payload);
		LOG.info("Got result - " + result);
	}

	/**
	 * 发送IOS消息
	 * @param appkey 
	 * 			    应用主键
	 * @param masterSecret 
	 * 			密钥
	 * @param title
	 *            消息标题
	 * @param subtitle
	 *            消息子标题
	 * @param content
	 *            消息内容
	 * @param alias
	 *            别名
	 */
	public static void sendIosAlert(String appkey,String masterSecret,String title, String subtitle, String content, String... alias) throws Exception{
		JPushClient jpushClient = new JPushClient(masterSecret.trim(), appkey.trim());

		IosAlert alert = IosAlert.newBuilder().setTitleAndBody(title, subtitle, content).setActionLocKey("PLAY")
				.build();
			PushResult result = jpushClient.sendIosNotificationWithAlias(alert, new HashMap<String, String>(), alias);
			LOG.info("Got result - " + result);
		
	}

	/**
	 * 发送带短信的消息
	 * @param appkey 
	 * 			    应用主键
	 * @param masterSecret 
	 * 			密钥
	 * @param title
	 *            消息标题
	 * @param smscontent
	 *            短信内容
	 * @param alias
	 *            别名
	 */
	public static void sendWithSMS(String appkey,String masterSecret,String title, String smscontent, String... alias) throws Exception{
		JPushClient jpushClient = new JPushClient(masterSecret.trim(), appkey.trim());
		SMS sms = SMS.content(smscontent, 10);
		PushResult result = jpushClient.sendAndroidMessageWithAlias(title, smscontent, sms, alias);
		LOG.info("Got result - " + result);

	}

	/**
	 * 获取clientId
	 * @param appkey 
	 * 			    应用主键
	 * @param masterSecret 
	 * 			密钥
	 * @param count
	 *            数量
	 * @param type
	 *            类型，如：push
	 */
	public static void getCidList(String appkey,String masterSecret,int count, String type) throws Exception{
		JPushClient jPushClient = new JPushClient(masterSecret.trim(), appkey.trim());
		CIDResult result = new CIDResult();
		if (type == null || type.equals("")) {
			result = jPushClient.getCidList(count, "push");
		} else {
			result = jPushClient.getCidList(count, type);
		}
		LOG.info("Got result - " + result);
		
	}

	/**
	 * 根据clientId推送消息
	 * @param appkey 
	 * 			    应用主键
	 * @param masterSecret 
	 * 			密钥
	 * @param content
	 *            消息内容
	 * @param cid
	 *            clientId
	 * @param registrationId
	 */
	public static void sendPushWithCid(String appkey,String masterSecret,String content, String cid, String... registrationId) throws Exception{
		JPushClient jPushClient = new JPushClient(masterSecret.trim(), appkey.trim());
		PushPayload pushPayload = buildPushObject_android_cid(content, cid, registrationId);
		PushResult result = jPushClient.sendPush(pushPayload);
		LOG.info("Got result - " + result);
		
	}

	/**
	 * 由josn对象内容发送消息
	 * @param appkey 
	 * 			    应用主键
	 * @param masterSecret 
	 * 			密钥 
	 * @param String
	 *            json
	 */
	public static void sendPush_fromJSON(String appkey,String masterSecret,String json) throws Exception{
		ClientConfig clientConfig = ClientConfig.getInstance();
		JPushClient jpushClient = new JPushClient(masterSecret.trim(), appkey.trim(), null, clientConfig);
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(PlatformNotification.class, new InterfaceAdapter<PlatformNotification>()).create();
		// Since the type of DeviceType is enum, thus the value should be
		// uppercase, same with the AudienceType.
		// String json =
		// "{\"platform\":{\"all\":false,\"deviceTypes\":[\"IOS\"]},\"audience\":{\"all\":false,\"targets\":[{\"audienceType\":\"TAG_AND\",\"values\":[\"tag1\",\"tag_all\"]}]},\"notification\":{\"notifications\":[{\"soundDisabled\":false,\"badgeDisabled\":false,\"sound\":\"happy\",\"badge\":\"5\",\"contentAvailable\":false,\"alert\":\"Test
		// from API Example -
		// alert\",\"extras\":{\"from\":\"JPush\"},\"type\":\"cn.jpush.api.push.model.notification.IosNotification\"}]},\"message\":{\"msgContent\":\"Test
		// from API Example -
		// msgContent\"},\"options\":{\"sendno\":1429488213,\"overrideMsgId\":0,\"timeToLive\":-1,\"apnsProduction\":true,\"bigPushDuration\":0}}";
		PushPayload payload = gson.fromJson(json, PushPayload.class);
		PushResult result = jpushClient.sendPush(payload);
		LOG.info("Got result - " + result);	
	}

	/**
	 * 发送2000条消息
	 * @param appkey 
	 * 			    应用主键
	 * @param masterSecret 
	 * 			密钥 
	 * @param content
	 *            消息内容
	 * @param extras
	 *            扩展
	 * @param tagValue
	 *            标签
	 */
	public static void sendPushes(String appkey,String masterSecret,String title,String content, Map<String, String> extras, String... tagValue) {
		ClientConfig clientConfig = ClientConfig.getInstance();
		final JPushClient jpushClient = new JPushClient(masterSecret.trim(), appkey.trim(), null, clientConfig);
		String authCode = ServiceHelper.getBasicAuthorization(appkey, masterSecret);
		// Here you can use NativeHttpClient or NettyHttpClient or
		// ApacheHttpClient.
		NativeHttpClient httpClient = new NativeHttpClient(authCode, null, clientConfig);
		// Call setHttpClient to set httpClient,
		// If you don't invoke this method, default httpClient will use
		// NativeHttpClient.
		// ApacheHttpClient httpClient = new ApacheHttpClient(authCode, null,
		// clientConfig);
		jpushClient.getPushClient().setHttpClient(httpClient);
		final PushPayload payload = buildPushObject_android_and_ios(title, content, extras);

		for (int i = 0; i < 10; i++) {
			Thread thread = new Thread() {
				public void run() {
					for (int j = 0; j < 200; j++) {
						long start = System.currentTimeMillis();
						try {
							PushResult result = jpushClient.sendPush(payload);
							LOG.info("Got result - " + result);

						} catch (APIConnectionException e) {
							LOG.error("Connection error. Should retry later. ", e);
							LOG.error("Sendno: " + payload.getSendno());

						} catch (APIRequestException e) {
							LOG.error("Error response from JPush server. Should review and fix it. ", e);
							LOG.info("HTTP Status: " + e.getStatus());
							LOG.info("Error Code: " + e.getErrorCode());
							LOG.info("Error Message: " + e.getErrorMessage());
							LOG.info("Msg ID: " + e.getMsgId());
							LOG.error("Sendno: " + payload.getSendno());
						}

						System.out
								.println("耗时" + (System.currentTimeMillis() - start) + "毫秒 sendCount:" + (++sendCount));
					}
				}
			};
			thread.start();
		}
	}

	/**
	 * 发送群组消息
	 * 
	 * @param title
	 *            消息标题
	 * @param content
	 *            消息内容
	 * @param extras
	 *            扩展
	 */
	public void sendGroupPush(String groupPushKey,String groupMasterSecret,String title, String content, Map<String, String> extras)throws Exception {
		GroupPushClient groupPushClient = new GroupPushClient(groupMasterSecret.trim(), groupPushKey.trim());
		final PushPayload payload = buildPushObject_android_and_ios(title, content, extras);		
		Map<String, PushResult> result = groupPushClient.sendGroupPush(payload);
		for (Map.Entry<String, PushResult> entry : result.entrySet()) {
			PushResult pushResult = entry.getValue();
			PushResult.Error error = pushResult.error;
			if (error != null) {
				LOG.info("AppKey: " + entry.getKey() + " error code : " + error.getCode() + " error message: "
						+ error.getMessage());
			} else {
				LOG.info(
						"AppKey: " + entry.getKey() + " sendno: " + pushResult.sendno + " msg_id:" + pushResult.msg_id);
			}

		}
	
	}

	/**
	 * 创建推送给所有设备的推送对象
	 * 
	 * @param content
	 *            消息内容
	 * @return
	 */
	public static PushPayload buildPushObject_all_all_alert(String content) {
		return PushPayload.alertAll(content);
	}

	/**
	 * 按别名推送消息
	 * @param content
	 * @param alias
	 * @return
	 */
	public static PushPayload buildPushObject_all_alias_alert(String content, String... alias) {
		return PushPayload.newBuilder().setPlatform(Platform.all()).setAudience(Audience.alias(alias))
				.setNotification(Notification.alert(content))
				.setMessage(Message.content(content)).setOptions(Options.newBuilder().setApnsProduction(true).build()).build();
	}

	/**
	 * 创建包含标签的Android平台推送对象
	 * 
	 * @param title
	 *            消息标题
	 * @param content
	 *            消息内容
	 * @param extras
	 *            扩展
	 * @param tagValue
	 *            标签
	 * @return
	 */
	public static PushPayload buildPushObject_android_tag_alertWithTitle(String title, String content,
			Map<String, String> extras, String... tagValue) {
		return PushPayload.newBuilder().setPlatform(Platform.android()).setAudience(Audience.tag(tagValue))
				.setNotification(Notification.android(content, title, extras != null ? extras : null)).build();
	}

	/**
	 * 创建多平台推送对象
	 * 
	 * @param title
	 *            消息标题
	 * @param content
	 *            消息内容
	 * @param extras
	 *            扩展
	 * @param alias
	 *            别名
	 * @return
	 */
	public static PushPayload buildPushObject_android_and_ios(String title, String content,
			Map<String, String> extras,String...alias) {

		return PushPayload.newBuilder().setPlatform(Platform.android_ios()).setAudience(Audience.alias(alias))
				.setNotification(Notification.newBuilder().setAlert(content)
						.addPlatformNotification(
								AndroidNotification.newBuilder().setTitle(title).addExtras(extras).build())
						.addPlatformNotification(IosNotification.newBuilder().incrBadge(1).addExtras(extras).build())
						.build())
				.build();
	}

	/**
	 * 创建推送对象
	 * 
	 * @param title
	 *            消息标题
	 * @param content
	 *            消息内容
	 * @param extras
	 *            扩展
	 * @param tagValue
	 *            标签
	 */
	public static void buildPushObject_with_extra(String title, String content, Map<String, String> extras,
			String... tagValue) {

		/*
		 * JsonObject jsonExtra = new JsonObject();
		 * jsonExtra.addProperty("extra1", 1); jsonExtra.addProperty("extra2",
		 * false);
		 * 
		 * Map<String, String> extras = new HashMap<String, String>();
		 * extras.put("extra_1", "val1"); extras.put("extra_2", "val2");
		 */

		PushPayload payload = PushPayload.newBuilder().setPlatform(Platform.android_ios())
				.setAudience(Audience.tag(tagValue))
				.setNotification(Notification.newBuilder().setAlert(content)
						.addPlatformNotification(AndroidNotification.newBuilder().setTitle(title).addExtras(extras)
								/*
								 * .addExtra("booleanExtra", false)
								 * .addExtra("numberExtra", 1)
								 * .addExtra("jsonExtra", jsonExtra)
								 */
								.build())
						.addPlatformNotification(IosNotification.newBuilder().incrBadge(1).addExtras(extras).build())
						.build())
				.build();

		System.out.println(payload.toJSON());
	}

	/**
	 * 创建包含标签的IOS平台推送对象
	 * 
	 * @param content
	 * @param extras
	 * @param tagValue
	 * @return
	 */
	public static PushPayload buildPushObject_ios_tagAnd_alertWithExtrasAndMessage(String content,
			Map<String, String> extras, String... tagValue) {
		return PushPayload.newBuilder().setPlatform(Platform.ios()).setAudience(Audience.tag_and(tagValue))
				.setNotification(Notification.newBuilder()
						.addPlatformNotification(IosNotification.newBuilder().setAlert(content).setBadge(5)
								.setSound("happy").addExtras(extras).build())
						.build())
				.setMessage(Message.content(content)).setOptions(Options.newBuilder().setApnsProduction(true).build())
				.build();
	}

	/**
	 * 创建包含图片的推送对象
	 * 
	 * @param title
	 *            消息标题
	 * @param content
	 *            消息内容
	 * @param bigPicPath
	 *            图片路径
	 * @param bigText
	 *            富文本信息
	 * @param category
	 *            类别
	 * @return
	 */
	public static PushPayload buildPushObject_android_newly_support(String title, String content, String bigPicPath,
			String bigText, String category) {
		JsonObject inbox = new JsonObject();
		inbox.add("line1", new JsonPrimitive("line1 string"));
		inbox.add("line2", new JsonPrimitive("line2 string"));
		inbox.add("contentTitle", new JsonPrimitive("title string"));
		inbox.add("summaryText", new JsonPrimitive("+3 more"));
		Notification notification = Notification.newBuilder()
				.addPlatformNotification(AndroidNotification.newBuilder().setAlert(content).setBigPicPath(bigPicPath)
						.setBigText(bigText).setBuilderId(1).setCategory(category).setInbox(inbox).setStyle(1)
						.setTitle(title).setPriority(1).build())
				.build();
		return PushPayload.newBuilder().setPlatform(Platform.all()).setAudience(Audience.all())
				.setNotification(notification)
				.setOptions(
						Options.newBuilder().setApnsProduction(true).setSendno(ServiceHelper.generateSendno()).build())
				.build();
	}

	/**
	 * 创建包含别名和标签的IOS平台推送对象
	 * 
	 * @param content
	 * @param tags
	 * @param alias
	 * @param extras
	 * @return
	 */
	public static PushPayload buildPushObject_ios_audienceMore_messageWithExtras(String content, List<String> tags,
			List<String> alias, Map<String, String> extras) {
		return PushPayload.newBuilder().setPlatform(Platform.android_ios())
				.setAudience(Audience.newBuilder().addAudienceTarget(AudienceTarget.tag(tags))
						.addAudienceTarget(AudienceTarget.alias(alias)).build())
				.setMessage(Message.newBuilder().setMsgContent(content).addExtras(extras).build()).build();
	}

	/**
	 * 创建不包含标签的推送对象
	 * 
	 * @param content
	 *            消息内容
	 * @param tagValue
	 *            不发送消息的标签value
	 * @return
	 */
	public static PushPayload buildPushObject_all_tag_not(String content, String... tagValue) {
		return PushPayload.newBuilder().setPlatform(Platform.all()).setAudience(Audience.tag_not(tagValue))
				.setNotification(Notification.alert(content)).build();
	}

	/**
	 * 根据clientId创建Android平台推送对象
	 * 
	 * @param content
	 * @param cid 	用于防止 api 调用端重试造成服务端的重复推送而定义的一个标识符
	 * @param registrationId 注册ID 设备标识。一次推送最多 1000 个
	 * @return
	 */
	public static PushPayload buildPushObject_android_cid(String content, String cid, String... registrationId) {
		return PushPayload.newBuilder().setPlatform(Platform.android())
				.setAudience(Audience.registrationId(registrationId)).setNotification(Notification.alert(content))
				.setCid(cid).build();
	}

}
