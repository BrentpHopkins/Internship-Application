import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
/**
 * The DataLoader class is responsible for loading all of the data from the
 * JSON files.
 * @author Brent Hopkins
 */
public class DataLoader extends DataConstants {
    
    /**
     * loads all users, joblistings, and company profiles from json
     * 
     * @return a searchable database containing all users and joblistings
     */
     public static SearchableDatabase loadData(){
         SearchableDatabase output = SearchableDatabase.getInstance();
         ArrayList<User> users = new ArrayList<>();
         ArrayList<JobListing> jobListings = new ArrayList<>();
         HashMap<String,CompanyProfile> companyProfiles = new HashMap<>();
         users.addAll(getAdmins());
         users.addAll(getStudents());
         jobListings.addAll(getJobListings(users));
         companyProfiles = getCompanyProfiles(jobListings);
         users.addAll(getEmployers(companyProfiles));
         output.setUsers(users);
         output.setJobListings(jobListings);
         return output;
     }
     /**
      * loads admin users from json
      * @return an ArrayList<User> containing all admins
      */
     private static ArrayList<User> getAdmins(){
        ArrayList<User> output = new ArrayList<User>();
     try {
        FileReader reader = new FileReader(ADMIN_FILE_NAME);
        JSONArray Admins =  (JSONArray)new JSONParser().parse(reader);
        for(int i = 0; i < Admins.size(); i++){
            JSONObject admin = (JSONObject)Admins.get(i);
            String firstName = (String)admin.get(ADMIN_FIRST_NAME);
            String lastName = (String)admin.get(ADMIN_LAST_NAME);
            String email = (String)admin.get(ADMIN_EMAIL);
            String password = (String)admin.get(ADMIN_PASSWORD);
            UUID id = UUID.fromString((String)admin.get(ADMIN_ID));
            output.add(new Administrator(firstName, lastName, email, password, id));
        }
     } catch (Exception e) {
         e.printStackTrace();
        }
        return output;
    }
    /**
     * loads students and their associated resumes from json
     * @return an ArrayList<User> conatining all students 
     */
    private static ArrayList<User> getStudents(){
        ArrayList<User> output = new ArrayList<User>();
        try {
           FileReader reader = new FileReader(STUDENT_FILE_NAME);
           JSONArray students =  (JSONArray)new JSONParser().parse(reader);
           for(int i = 0; i < students.size(); i++){
               JSONObject student = (JSONObject)students.get(i);
               String firstName = (String)student.get(STUDENT_FIRST_NAME);
               String lastName = (String)student.get(STUDENT_LAST_NAME);
               String email = (String)student.get(STUDENT_EMAIL);
               String password = (String)student.get(STUDENT_PASSWORD);
               String yearInSchool = (String)student.get(STUDENT_YEAR_IN_SCHOOL);
               UUID id = UUID.fromString((String)student.get(STUDENT_ID));
               ArrayList<Review> reviews = getReviews(student);
               ArrayList<Resume> resumes = getResumes(student);
               Student temp = new Student(firstName, lastName, email, password, yearInSchool, id);
               temp.setReviews(reviews);
               temp.setResume(resumes.get(0));
               output.add(temp);

           }
        } catch (Exception e) {
            e.printStackTrace();
           }
           return output;
    }

    /**
     * The getJobListings method loads all of the JobListings from the data
     * files into an arraylist of JobListings.
     * @return The ArrayList of JobListings loaded from the JSON files
     */
    private static ArrayList<JobListing> getJobListings(ArrayList<User> users){
        ArrayList<JobListing> output = new ArrayList<>();
        HashMap<String,User> usersMap = new HashMap<>();
        for(User data: users){
            usersMap.put(data.getUUID().toString(), data);
        }
        try {
            FileReader reader = new FileReader(JOB_LISTING_FILE_NAME);
            JSONArray listings =  (JSONArray)new JSONParser().parse(reader);
            for(int i = 0; i < listings.size(); i++){
                JSONObject listing = (JSONObject)listings.get(i);
                JSONArray applicantIDS = (JSONArray)listing.get(LISTING_APPLICANT_IDS);
                ArrayList<Student> applicants = getapplicants(usersMap, applicantIDS);
                String title = (String)listing.get(LISTING_TITLE);
                String description = (String)listing.get(LISTING_DESCRIPTION);
                String companyName = (String)listing.get(LISTING_COMPANY_NAME);
                Boolean paid = (Boolean)listing.get(LISTING_PAID);
                Double payRate = (Double)listing.get(LISTING_PAY_RATE);
                UUID id = UUID.fromString((String)listing.get(LISTING_ID));
                ArrayList<String> requiredSkills = getRequiredSkills(listing);
                JobListing temp = new JobListing(companyName, title, description, paid, payRate, id, applicants, requiredSkills);
                output.add(temp);
                
            }
         } catch (Exception e) {
             e.printStackTrace();
            }
            return output;
        }
        /**
         * loads all companyprofiles from json
         * @param jobListings a hashmap of all joblistings. Used to associate listings with the companyprofile
         * @return a hashmap containing all companyprofiles
         */
        private static HashMap<String,CompanyProfile> getCompanyProfiles(ArrayList<JobListing> jobListings){
        HashMap<String,CompanyProfile> output = new HashMap<>();
        HashMap<String,JobListing> listingMap = new HashMap<>();
        for(JobListing listing:jobListings){
            listingMap.put(listing.getUUID().toString(), listing);
        }
        try {
            FileReader reader = new FileReader(COMPANY_PROFILE_FILE_NAME);
            JSONArray CompanyProfiles =  (JSONArray)new JSONParser().parse(reader);
            for(int i = 0; i < CompanyProfiles.size()-1; i++){
                JSONObject companyprofile = (JSONObject)CompanyProfiles.get(i);
                JSONArray listingIDS = (JSONArray)companyprofile.get(COMPANY_LISTINGS_IDS);
                String companyName = (String)companyprofile.get(COMPANY_NAME);
                String hqAddress = (String)companyprofile.get(COMPANY_HQ_ADDRESS);
                String description = (String)companyprofile.get(COMPANY_DESCRIPTION);
                UUID companyID = UUID.fromString((String)companyprofile.get(COMPANY_ID));
                ArrayList<Review> reviews = getReviews(companyprofile);
                ArrayList<JobListing> listings = getCurrentListings(listingMap, listingIDS);
                CompanyProfile temp = new CompanyProfile(companyName, hqAddress, description, companyID, reviews, listings);
                output.put(temp.getUUID().toString(), temp);
                //todo add listings 
             
                
            }
         } catch (Exception e) {
             e.printStackTrace();
            }
            return output;
    }

    /**
     * loads all employer accounts from json
     * @param companyProfiles a hashmap of companyprofiles. Used to associate employers with companyprofiles
     * @return and ArrayList<Employer> containing all employers
     */
     private static ArrayList<Employer> getEmployers(HashMap<String,CompanyProfile> companyProfiles){
        ArrayList<Employer> output = new ArrayList<>();
        try {
            FileReader reader = new FileReader(EMPLOYER_FILE_NAME);
            JSONArray employers =  (JSONArray)new JSONParser().parse(reader);
            for(int i = 0; i < employers.size(); i++){
                JSONObject employer = (JSONObject)employers.get(i);
                String firstName = (String)employer.get(EMPLOYER_FIRST_NAME);
                String lastName = (String)employer.get(EMPLOYER_LAST_NAME);
                String email = (String)employer.get(EMPLOYER_EMAIL);
                String password = (String)employer.get(EMPLOYER_EMAIL);
                UUID id = UUID.fromString((String)employer.get(EMPLOYER_ID));
                CompanyProfile associatedCompany = companyProfiles.get(employer.get(EMPLOYER_ASSOCIATED_COMPANY));
                output.add(new Employer(firstName, lastName, email, password, associatedCompany, id));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
     }
     /**
      * loads student resumes
      * @param student the student json object containing the resumes
      * @return an ArrayList<Resume> containing all the students resumes
      */
    private static ArrayList<Resume> getResumes(JSONObject student){
        ArrayList<Resume> output = new ArrayList<>();
        JSONArray resumes = (JSONArray)student.get(STUDENT_RESUMES);
        for(int i = 0; i < resumes.size() && resumes.size() != 0; i++){
            JSONObject resume = (JSONObject)resumes.get(i);
            String yearInSchool = (String)student.get(STUDENT_YEAR_IN_SCHOOL);
            ArrayList<String> classes = getClasses(resume, student);
            ArrayList<String> skills = getSkills(resume, student);
            ArrayList<Education> education = getEducation(resume);
            ArrayList<WorkExperience> workExperiences = getWorkExperiences(resume);
            output.add(new Resume(yearInSchool, skills, education, workExperiences, classes));
        }
        return output;
    }
    /**
     * loads the students selected skills for the resume
     * @param resume the resume json object being loaded
     * @param student the student json object whos resume is being loaded 
     * @return an ArrayList<String> containing the selected skills for the resume
     */
    private static ArrayList<String> getSkills(JSONObject resume, JSONObject student){
        ArrayList<String> output = new ArrayList<>();
        JSONArray skillindex = (JSONArray)resume.get(RESUME_SKILL_INDEXES);
        JSONArray skills = (JSONArray)student.get(STUDENT_SKILLS);
        for(int i = 0; i < skillindex.size() && skills.size() != 0; i++){
            int index = ((Long)skillindex.get(i)).intValue();
            output.add((String)skills.get(index));
        }
        return output;
    }
    /**
     * loads the education section of a students resume
     * @param resume the resume json object being read
     * @return an ArrayList<Education> containing all of the education experiences from the resume json object
     */
    private static ArrayList<Education> getEducation(JSONObject resume){
        ArrayList<Education> output = new ArrayList<>();
        JSONArray educationExperiences = (JSONArray)resume.get(RESUME_EDUCATION);
        for(int i = 0; i < educationExperiences.size() && educationExperiences.size() != 0; i++){
            JSONObject educationExperience = (JSONObject)educationExperiences.get(i);
            String nameofUniversity = (String)educationExperience.get(EDUCATION_UNIVERSITY_NAME);
            String major = (String)educationExperience.get(EDUCATION_MAJOR);
            Double gpa = (Double)educationExperience.get(EDUCATION_GPA);
            String expectedGradDate = (String)educationExperience.get(EDUCATION_GRADUATION_DATE);
            Education temp = new Education(nameofUniversity, major, gpa, expectedGradDate);
            output.add(temp);
        }
        return output;
    }
    /**
     * loads the classes selected by the student for the resume
     * @param resume the resume json object being loaded
     * @param student the student json object containing the list of skills posssiable to be selected 
     * @return an ArrayList<String> containing all the skills selected for the resume
     */
    private static ArrayList<String> getClasses(JSONObject resume, JSONObject student){
        ArrayList<String> output = new ArrayList<>();
        JSONArray classNames = (JSONArray)student.get(STUDENT_CLASSES);
        JSONArray classIndexes = (JSONArray)resume.get(RESUME_CLASS_INDEXES);
        for(int i = 0; i < classIndexes.size() && classNames.size() != 0; i++){
            int index = ((Long)classIndexes.get(i)).intValue() ;
            output.add((String)classNames.get(index));
        }
        return output;
    }
    /**
     * loads a resumes work experiences 
     * @param resume the resume being loaded
     * @return an ArrayList<WorkExperience> containing all the work experiences listed in the resume
     */
    private static ArrayList<WorkExperience> getWorkExperiences(JSONObject resume){
        ArrayList<WorkExperience> output = new ArrayList<>();
        JSONArray workExperiences = (JSONArray)resume.get(RESUME_WORK_EXPERIENCE);
        for(int i = 0; i < workExperiences.size() && workExperiences.size() != 0; i++){
            JSONObject workExperience = (JSONObject)workExperiences.get(i);
            String jobTitle = (String)workExperience.get(EXPERIENCE_TITLE);
            String company = (String)workExperience.get(EXPERIENCE_COMPANY);
            String dateRange = (String)workExperience.get(EXPERIENCE_DATE_RANGE);
            String description = (String)workExperience.get(EXPERIENCE_DESCRIPTION);
            WorkExperience temp = new WorkExperience(jobTitle, company, dateRange, description);
            output.add(temp);
        }
        return output;
    }

    /**
     * loads all reviews associated with the "reviewed" json object
     * @param reviewed the Json object having its reviews loaded
     * @return an ArrayList<Review> containing all the reviews of the "reviewed" Json object
     */
    private static ArrayList<Review> getReviews(JSONObject reviewed){
        ArrayList<Review> output = new ArrayList<>();
        JSONArray reviews = (JSONArray)reviewed.get("reviews");
        for(int i = 0; i < reviews.size() && reviews.size() != 0; i++){
            JSONObject review = (JSONObject)reviews.get(i);
            String firstName = (String)review.get(REVIEW_FIRST_NAME);
            String lastName = (String)review.get(REVIEW_LAST_NAME);
            String message = (String)review.get(REVIEW_MESSAGE);
            int rating = ((Long)review.get(REVIEW_RATING)).intValue();
            output.add(new Review(firstName, lastName, rating, message));
        }
        return output;
    }
    /**
     * loads the applicant list for a joblisting
     * @param users a hashmap of all users. used to associate applicants with listings 
     * @param applicantIDS the uuids of the listings applicants 
     * @return an ArrayList<Student> containing all of the listings applicants 
     */
    private static ArrayList<Student> getapplicants(HashMap<String,User> users, JSONArray applicantIDS){
        ArrayList<Student> applicants = new ArrayList<>();
        for(int i = 0; i < applicantIDS.size(); i++){
            applicants.add((Student)users.get(applicantIDS.get(i)));
        }
        return applicants;
    }
    /**
     * loads a listings required skills
     * @param listing the listing being loaded
     * @return an ArrayList<String> containing all of the listings required skills
     */
    private static ArrayList<String> getRequiredSkills(JSONObject listing){
        ArrayList<String> output = new ArrayList<>();
        JSONArray requiredSkills = (JSONArray) listing.get(LISTING_REQUIRED_SKILLS);
        for(int i = 0; i < requiredSkills.size() && requiredSkills.size() != 0; i++){
            output.add((String)requiredSkills.get(i));
        }
        return output;
    }
    /**
     * loads a company profiles current joblistings
     * @param listingmap a hasmap of all listings. used to associate listings with company profiles
     * @param listingIDS the ids of listings associated with the company profile being loaded
     * @return an ArrayList<JobListing> containing all joblistings associated with a company profile
     */
    private static ArrayList<JobListing> getCurrentListings(HashMap<String,JobListing> listingmap, JSONArray listingIDS){
        ArrayList<JobListing> output = new ArrayList<>();
        
        for(int i = 0; i < listingIDS.size(); i++){
            output.add(listingmap.get(listingIDS.get(i)));
        }
        return output;
    }
}
//pain