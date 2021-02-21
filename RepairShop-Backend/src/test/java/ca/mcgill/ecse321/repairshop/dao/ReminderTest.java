package ca.mcgill.ecse321.repairshop.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.security.Timestamp;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ca.mcgill.ecse321.repairshop.model.Customer;
import ca.mcgill.ecse321.repairshop.model.Reminder;
import ca.mcgill.ecse321.repairshop.model.ReminderType;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ReminderTest {

	@Autowired
	private ReminderRepository reminderRepository;
		
	@BeforeEach
	@AfterEach
	public void clearDatabase() {
		reminderRepository.deleteAll();
	}
	
	@Test
	public void testPersistAndLoadReminder() {
		
		// create customer
		String customerName = "Solomina XXukld";
		String customerEmail = "Solomin@gmail.com";
		String customerPassword = "scrum?meeting?101";
		String customerAddress = "2 UpInTheAir Street";
		String customerPhone = "333-445-3567";
		
		Customer customer = new Customer();
		
		customer.setName(customerName);
		customer.setAddress(customerAddress);
		customer.setEmail(customerEmail);
		customer.setPassword(customerPassword);
		customer.setPhoneNumber(customerPhone);
		
		// create reminder ID
		long reminderID = 999234;
		
		// create reminder time
		java.sql.Timestamp reminderDay = java.sql.Timestamp.valueOf("2021-04-20 10:10:10.0");
		
		// TODO a whole lot of problems with this enum thing. Fix later
		// create oil change reminder
		ReminderType reminderType = ReminderType.OilChange;
		
		// create reminder
		Reminder reminder = new Reminder();
		reminder.setCustomer(customer);
		reminder.setReminderID(reminderID);
		reminder.setDateTime(reminderDay);
		reminder.setReminderType(reminderType);
		
		reminder = null;
		
		reminder = reminderRepository.findByCustomerAndType(customer, reminderType);
		assertNotNull(reminder);
		assertEquals(reminderID, reminder.getReminderID());
		assertEquals(customerName, reminder.getCustomer().getName());
		assertEquals(reminderType, reminder.getReminderType());
		
	}
}