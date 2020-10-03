package com.stockAnalysis.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.stockAnalysis.domain.User;
import com.stockAnalysis.domain.UserRepository;

import net.bytebuddy.agent.builder.AgentBuilder.InitializationStrategy.SelfInjection.Split;

import java.io.IOException;
import java.util.Iterator;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Controller
public class UserController {
	private List<User> users = new ArrayList<User>();

	@Autowired
	private UserRepository userRepository;

	@PostMapping("/create")
	public String create(User user) {
		users.add(user);
		userRepository.save(user);
		return "redirect:/list";
	}

	@GetMapping("/list")
	public String list(Model model) {
		model.addAttribute("users", userRepository.findAll());
		return "list";
	}

	@GetMapping("/users/{id}/form")
	public String updateForm(@PathVariable String id, Model model) {
		model.addAttribute("id", userRepository.findById(id).get().getId());
		model.addAttribute("name", userRepository.findById(id).get().getName());
		model.addAttribute("pwd", userRepository.findById(id).get().getPassword());

		return "user/updateForm";
	}

	@PostMapping("/users/{id}/modify")
	public String modifyUser(@PathVariable String id, User newUser) {
		User user = userRepository.findById(id).get();
		user.update(newUser);
		userRepository.save(user);
		return "redirect:/list";
	}

	@PostMapping("/login")
	public String login(String id, String password, Model model) {
		User user;
		try {
			user = userRepository.findById(id).get();
		} catch (Exception e) {
			// TODO: handle exception
			return "redirect:/login.html";
		}

		return "redirect:/index.html";
	}

	@GetMapping("/search")
	public String search(String subject, Model model) throws IOException {
		model.addAttribute("subject", subject);
		Calendar cal = Calendar.getInstance();

		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;

		String site = "https://markets.financialcontent.com/stocks/quote/historical?Range=12&Symbol=" + subject
				+ "&Month=" + month + "&Year=" + year;

		try {

			Connection.Response res = Jsoup.connect(site).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
					.execute();

			if (res.statusCode() == 200) {
				Document doc = Jsoup.connect(site).get();
				Elements eles = doc.select("tbody > tr > td");

				int cnt = 0;
				float max_profit = 0;
				float[] daily_high = new float[107];
				float[] daily_low = new float[107];
				int idx1 = 0;
				int idx2 = 0;
				float price = 0;
				for (Element ele : eles) {
					if (cnt == 749) {
						break;
					}

					String temp = ele.toString();
					temp = temp.substring(4);
					temp = temp.substring(0, temp.length() - 5);

					if ((cnt % 7 == 2) || (cnt % 7 == 3)) {
						price = Float.valueOf(temp);
					}

					if (cnt % 7 == 2) {
						daily_high[idx1++] = price;

					}

					if (cnt % 7 == 3) {
						daily_low[idx2++] = price;

					}

					cnt++;

				}

				// 최대 이윤 계산

				int len = daily_high.length;
				max_profit = daily_high[len - 2] - daily_low[len - 1];

				float min_element = daily_low[len - 1];
				for (int i = len - 3; i >= 0; i--) {
					if (daily_high[i] - min_element > max_profit) {
						max_profit = daily_high[i] - min_element;
					}

					if (daily_low[i] < min_element) {

						min_element = daily_low[i];
					}
				}

				// 손실 방지
				if (max_profit < 0.0) {
					max_profit = 0;
				}

				model.addAttribute("max_profit", max_profit);

			} else {
				int temp = -1;
				model.addAttribute("max_profit", temp);
			}

		} catch (IOException e) {
			e.printStackTrace();

		}

		return "index";
	}
}
