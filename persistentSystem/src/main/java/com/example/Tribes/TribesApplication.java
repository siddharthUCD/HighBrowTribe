package com.example.Tribes;

import com.example.Tribes.Model.Questions;
import com.example.Tribes.Model.TribeQuestionDetails;
import com.example.Tribes.Model.User;
import com.example.Tribes.Repo.Constants;
import com.example.Tribes.Repo.QuestionsRepo;
import com.example.Tribes.Repo.TribeQuestionRepo;
import com.example.Tribes.Repo.UserRepo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import service.centralCore.*;
import service.messages.TriberInitializationResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

/**
 * Spring boot application to start the Database and manage records in DB
 */
@SpringBootApplication
public class TribesApplication {

	public static void main(String[] args) {
		Constants.configurableApplicationContext = SpringApplication.run(TribesApplication.class, args);
		addQuestions();
	}

	/**
	 * Adds questions in DB if not present
	 */
	public static void addQuestions(){
		QuestionsRepo questionsRepo = Constants.configurableApplicationContext.getBean(QuestionsRepo.class);
		//stores data in questions class if empty, may happen when the in-memory db is deleted
		ArrayList<Questions> questionsList = (ArrayList<Questions>) questionsRepo.findAll();
		if(questionsList.size() == 0) {
			File myObj = new File(Constants.questionsFile);
			Scanner myReader = null;
			try {
				myReader = new Scanner(myObj);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			while (myReader.hasNextLine()) {
				String[] data = myReader.nextLine().split("/");
				Questions questions = new Questions(Integer.parseInt(data[0]), data[1]);
				questionsRepo.save(questions);
			}
		}
	}

	/**
	 * Returns all programming challange questions
	 *
	 * @return - questions list
	 */
	public static ArrayList<Questions> getQuestion(){
		QuestionsRepo questionsRepo = Constants.configurableApplicationContext.getBean(QuestionsRepo.class);
		return (ArrayList<Questions>) questionsRepo.findAll();
	}

	/**
	 * Fetches the tribe question object by tribe id
	 *
	 * @param tribeId - tribe id
	 * @return - tribe question detail object
	 */
	public static TribeQuestionDetails getTribeQuestionDetails(long tribeId){
		TribeQuestionRepo tribeQuestionRepo = Constants.configurableApplicationContext.getBean(TribeQuestionRepo.class);
		return tribeQuestionRepo.findById(tribeId).orElse(null);
	}

	/**
	 * Deletes the questions which is completed by a tribe
	 *
	 * @param tribeId - tribe id
	 */
	public static void deleteTribeQuestionDetails(final Long tribeId){
		TribeQuestionRepo tribeQuestionRepo = Constants.configurableApplicationContext.getBean(TribeQuestionRepo.class);
		tribeQuestionRepo.deleteById(tribeId);
	}

	/**
	 * Makes an entry in tribeQuestionDetails table when a tribe starts working on a programming challange
	 *
	 * @param tribeId - tribe id
	 * @param question - question
	 */
	public static void setTribeQuestionDetails(final Long tribeId, final String question){
		TribeQuestionRepo tribeQuestionRepo = Constants.configurableApplicationContext.getBean(TribeQuestionRepo.class);
		TribeQuestionDetails tribeQuestionDetails = new TribeQuestionDetails(tribeId,question,new Timestamp(System.currentTimeMillis()));
		tribeQuestionRepo.save(tribeQuestionDetails);
	}

	/**
	 * Persists the user details to User table
	 *
	 * @param uniqueId - unique id
	 * @param userInfo - user info
	 * @param tribeLanguage - tribe language
	 */
	public static void setUserInfo(final Long uniqueId,final UserInfo userInfo,final String tribeLanguage){
		UserRepo userRepo = Constants.configurableApplicationContext.getBean(UserRepo.class);
		User user = new User(uniqueId,userInfo.getName(),userInfo.getTribeId(), userInfo.getInterests().getProgrammingLanguages().stream().collect(Collectors.joining(",")),userInfo.getGitHubId(),tribeLanguage,userInfo.getPortNumber());

		userRepo.save(user);
	}

	/**
	 * Updates user details for selected user
	 *
	 * @param newId - user id
	 * @param newUser - user object
	 * @return - user info
	 */
	public static UserInfo updateUserInfo(long newId, UserInfo newUser){
		UserRepo userRepo = Constants.configurableApplicationContext.getBean(UserRepo.class);

		ArrayList<User> users = (ArrayList<User>) userRepo.findAll();

		User oldUser = users.stream().filter(user->user.getGitHubId().equals(newUser.getGitHubId())).collect(Collectors.toList()).get(0);
		userRepo.deleteById(oldUser.getUniqueId());

		oldUser.setUniqueId(newId);
		oldUser.setPortNumber(newUser.getPortNumber());
		userRepo.save(oldUser);

		UserInfo user = new UserInfo(newUser.getName(), newUser.getGitHubId());
		user.setInterests(newUser.getInterests());
		user.setPortNumber(newUser.getPortNumber());
		user.setTribeId(newUser.getTribeId());

		return user;
	}

	/**
	 * Gets all user details from database and sends to triber when system starts.
	 * The data send helps triber to know the current state of all the tribes
	 * Initializes all tribes and users details in triberInitializationResponse
	 */
	public static TriberInitializationResponse getAllUserInfo() {
		UserRepo userRepo = Constants.configurableApplicationContext.getBean(UserRepo.class);
		ArrayList<User> users = (ArrayList<User>) userRepo.findAll();
		ArrayList<UserInfo> allUsers = new ArrayList<>();
		users.forEach(user->{
			UserInfo mappedUserInfo = new UserInfo();
			mappedUserInfo.setName(user.getName());
			mappedUserInfo.setGitHubId(user.getGitHubId());
			mappedUserInfo.setPortNumber(user.getPortNumber());
			mappedUserInfo.setTribeId(user.getTribeId());

			Set<String> interestsSet = Stream.of(user.getProgrammingLanguage().trim().split("\\s*,\\s*"))
					.collect(Collectors.toSet());

			mappedUserInfo.setInterests(new Interests(interestsSet));
			allUsers.add(mappedUserInfo);
		});
		if (users.size() > 0) {
			ArrayList<Tribe> tribeArrayList = new ArrayList<>();
			Map<Long, List<User>> tribeAndUserMap = users.stream().collect(groupingBy(User::getTribeId));

			tribeAndUserMap.forEach((k, v) ->
			{
				List<UserInfo> userInfoListTemp = new ArrayList<>();
				v.forEach(item -> {
					UserInfo userInfo = new UserInfo(item.getName(), item.getGitHubId());
					userInfo.setPortNumber(item.getPortNumber());
					userInfo.setTribeId(item.getTribeId());
					userInfoListTemp.add(userInfo);

				});

				String tribeProgrammingLanguages= String.join(",",Stream.of(v.stream().map(User::getProgrammingLanguage)
								.collect(Collectors.joining(",")).trim().split("\\s*,\\s*"))
						.collect(Collectors.toSet()));

				Tribe t = new Tribe(k, v.get(0).getTribeLanguage(), tribeProgrammingLanguages, userInfoListTemp);
				tribeArrayList.add(t);
			});

			Long maxTribeId = users.stream().max(Comparator.comparing(User::getTribeId)).orElseThrow().getTribeId();
			Long maxUniqueId = users.stream().max(Comparator.comparing(User::getUniqueId)).orElseThrow().getUniqueId();

			return new TriberInitializationResponse(allUsers,
					tribeArrayList, maxUniqueId, maxTribeId);
		} else {
			return new TriberInitializationResponse(null,
					new ArrayList<Tribe>(), 0L, 0L);
		}
	}
}