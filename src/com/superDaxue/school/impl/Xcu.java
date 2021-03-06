package com.superDaxue.school.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import net.sf.json.JSONObject;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.htmlparser.util.ParserException;

import com.superDaxue.login.IRequest;
import com.superDaxue.login.impl.HttpClientRequest;
import com.superDaxue.parse.ParseTool;
import com.superDaxue.school.ISchool;
import com.superDaxue.tool.DateTool;

public class Xcu implements ISchool{
	private String username;
	private String un_cookie;
	private IRequest requestclient = new HttpClientRequest();
	private ParseTool parseTool=new ParseTool();
	private String baseurl="211.67.191.45";
	
	public String getCheckNum(String savePath) {
	
		String hostUrl="";
		try {
			hostUrl=requestclient.getGetUrl(null, "http://"+baseurl+"/");
			un_cookie=hostUrl.substring(hostUrl.indexOf("("),hostUrl.indexOf(")")+1);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		
		String n = new Random().nextInt(100000)+""+new Date().getTime();
		String image_save_path = savePath+ "checkNum" + n + ".jpg";
		// 处理验证url
		String img_path_url = "http://"+baseurl+"/"+un_cookie+"/CheckCode.aspx?rand=" + n;
		requestclient.getCodeCookie(img_path_url, image_save_path);
		return n;
	}
	
	
	public JSONObject doLogin(String username, String password,String checkNum) {
		
		JSONObject jsonObject=new JSONObject();
		this.username=username;
		String path_index="http://"+baseurl+"/"+un_cookie+"/default2.aspx";
		List<NameValuePair> headers= new ArrayList<NameValuePair>();
		String html;
		try {
			html = requestclient.doGet(headers, path_index);
			headers= parseTool.parseCoursesParam(html);
		} catch (Exception e1) {
			e1.printStackTrace();
			jsonObject.put("message", "网络异常请稍后再试");
			return jsonObject;
		}
		String login_url="http://"+baseurl+"/"+un_cookie+"/default2.aspx";
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("txtUserName", username));
		nvps.add(new BasicNameValuePair("TextBox2", password));
		nvps.add(new BasicNameValuePair("txtSecretCode", checkNum));
		nvps.add(new BasicNameValuePair("RadioButtonList1", ""));
		nvps.add(new BasicNameValuePair("Button1", ""));
		nvps.add(new BasicNameValuePair("lbLanguage", ""));
		nvps.add(new BasicNameValuePair("hidPdrs", ""));
		nvps.add(new BasicNameValuePair("hidsc", ""));
		nvps.addAll(headers);
		
		List<NameValuePair> mapHeader=new ArrayList<NameValuePair>();
		mapHeader.add(new BasicNameValuePair("Host",baseurl));
		mapHeader.add(new BasicNameValuePair("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
		mapHeader.add(new BasicNameValuePair("Referer","http://"+baseurl+"/default2.aspx"));
		mapHeader.add(new BasicNameValuePair("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36"));
//		mapHeader.add(new BasicNameValuePair("Cookie",imgCookie));
		String temp="";
		try {
			temp = requestclient.doPost(mapHeader, nvps, login_url);
		} catch (Exception e) {
			e.printStackTrace();
			jsonObject.put("message", "网络异常，登录错误");
		}	
		if(temp.indexOf("验证码不正确")!=-1){
			jsonObject.put("message", "验证码不正确");
		}else 
		if(temp.indexOf("Object moved to")!=-1){
			jsonObject.put("result","成功！");
			jsonObject.put("isSuccess","1");
		}
		else{
			jsonObject.put("message", "登录失败请检查您的用户名和密码");
		}
		return jsonObject;
	}
	
	public String getScore() {
		String path="http://"+baseurl+"/"+un_cookie+"/xscj.aspx?xh="+username+"&xm=%CD%F5%CB%C9%B7%C9&gnmkdm=N121608";
		List<NameValuePair> mapHeader=new ArrayList<NameValuePair>();
		mapHeader.add(new BasicNameValuePair("Host",baseurl));
		mapHeader.add(new BasicNameValuePair("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
		mapHeader.add(new BasicNameValuePair("Referer","http://"+baseurl+"/xscj.aspx?xh="+username+"&xm=%CD%F5%CB%C9%B7%C9&gnmkdm=N121608"));
		mapHeader.add(new BasicNameValuePair("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36"));
	//	mapHeader.add(new BasicNameValuePair("Cookie",cookie));
		String str="";
		try {
			str = requestclient.doGet(mapHeader,path);
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
		try {
			int yearStudent=parseTool.parseYear(str);
			if(yearStudent==-1){
				return "";
			}
			List<String> yearlist=new DateTool().getYearList(yearStudent);
			StringBuffer resultTemp=new StringBuffer();
			int i=0;
			for(String yearItem:yearlist){
				if(i>8){
					break;
				}
				String[] arr=yearItem.split(",");
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("txtQSCJ", "0"));
				nvps.add(new BasicNameValuePair("txtZZCJ", "100"));
				nvps.add(new BasicNameValuePair("Button1", ""));
				nvps.add(new BasicNameValuePair("ddlXN", arr[0]));
				nvps.add(new BasicNameValuePair("ddlXQ", arr[1]));
				nvps.addAll(parseTool.parseCoursesParam(str));
				try {
					String temp=requestclient.doPost(mapHeader,nvps,path);
					resultTemp.append(temp);					
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
				i++;
			}
			return resultTemp.toString();
		} catch (ParserException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getTimetable() {
		String path="http://"+baseurl+"/"+un_cookie+"/xskbcx.aspx?xh="+username+"&xm=%CD%F5%B7%A8&gnmkdm=N121603";
		List<NameValuePair> mapHeader=new ArrayList<NameValuePair>();
		mapHeader.add(new BasicNameValuePair("Host",baseurl));
		mapHeader.add(new BasicNameValuePair("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
		mapHeader.add(new BasicNameValuePair("Referer","http://"+baseurl+"/xs_main.aspx?xh="+username));
		mapHeader.add(new BasicNameValuePair("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36"));
//		mapHeader.add(new BasicNameValuePair("Cookie",cookie));
		try {
			String temp=requestclient.doGet(mapHeader,path);
			return temp;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
