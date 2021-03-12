package ca.mcgill.ecse321.repairshop.service;

import ca.mcgill.ecse321.repairshop.dto.AppointmentDto;
import ca.mcgill.ecse321.repairshop.model.*;
import ca.mcgill.ecse321.repairshop.repository.*;
import ca.mcgill.ecse321.repairshop.service.utilities.SystemTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class TestAppointmentService {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private TechnicianRepository technicianRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private AppointmentService appointmentService;


    // Test data - only using what is needed for the tests

    // Going to use times relative to 2021-03-01 00:00:00.0 to make them easier to understand
    private static final LocalDateTime INITIAL_TIME = LocalDateTime.parse("2021-03-01T00:00:00.0"); // Monday

    // Target appointment start time
    private static final String APP_START_TIME = Timestamp.valueOf(INITIAL_TIME.plusDays(14).plusHours(9)).toString(); // Monday
    private static final String APP_START_TIME2 = Timestamp.valueOf(INITIAL_TIME.minusDays(14)).toString(); // Past time
    private static final String APP_START_TIME3 = Timestamp.valueOf(INITIAL_TIME.plusDays(14).plusHours(13)).toString(); // Tuesday - end time out of hours
    private static final String APP_START_TIME4 = Timestamp.valueOf(INITIAL_TIME.plusDays(18).plusHours(7)).toString(); // Friday - outside hours
    private static final String APP_START_TIME5 = Timestamp.valueOf(INITIAL_TIME.plusDays(15).plusHours(7)).toString(); // Wednesday - during holiday
    private static final String APP_START_TIME6 = Timestamp.valueOf(INITIAL_TIME.plusDays(14).plusHours(11)).toString(); // Tuesday - overlaps another appointment
    private static final String APP_START_TIME7 = Timestamp.valueOf(INITIAL_TIME.plusDays(14).plusHours(13)).toString(); // Tuesday - during another appointment

    // Service
    private static final String SERVICE_NAME = "Service";
    private static final int SERVICE_DURATION = 4;
    private static final double SERVICE_PRICE = 49.99;

    // Technicians
    private static final String TECHNICIAN_EMAIL = "technician@mail.com";
    private static final String TECHNICIAN_EMAIL2 = "technician2@mail.com";
    private static final Timestamp HOURS_START = Timestamp.valueOf(INITIAL_TIME.plusHours(8)); // Monday
    private static final Timestamp HOURS_END = Timestamp.valueOf(INITIAL_TIME.plusHours(14)); // Monday
    private static final Timestamp HOURS_START2 = Timestamp.valueOf(INITIAL_TIME.plusDays(1).plusHours(10)); // Tuesday
    private static final Timestamp HOURS_END2 = Timestamp.valueOf(INITIAL_TIME.plusDays(1).plusHours(18)); // Tuesday
    private static final Timestamp HOURS_START3 = Timestamp.valueOf(INITIAL_TIME.plusDays(2).plusHours(18)); // Wednesday
    private static final Timestamp HOURS_END3 = Timestamp.valueOf(INITIAL_TIME.plusDays(2).plusHours(18)); // Wednesday
    private static final Timestamp APP_START = Timestamp.valueOf(INITIAL_TIME.plusDays(14).plusHours(12)); // Tuesday
    private static final Timestamp APP_END = Timestamp.valueOf(INITIAL_TIME.plusDays(14).plusHours(15)); // Tuesday

    // Customer
    private static final String CUSTOMER_EMAIL = "customer@mail.com";

    // Business
    private static final String BUSINESS_NAME = "Business";
    private static final Timestamp HOLIDAY_START = Timestamp.valueOf(INITIAL_TIME.plusDays(15)); // Wednesday
    private static final Timestamp HOLIDAY_END = Timestamp.valueOf(INITIAL_TIME.plusDays(15).plusHours(23)); // Wednesday


    @BeforeEach
    public void setMockOutput() {

        SystemTime.setTest(true);
        SystemTime.setTestTime(Timestamp.valueOf(INITIAL_TIME.plusDays(11)));

        lenient().when(serviceRepository.findServiceByName(any(String.class))).thenAnswer((InvocationOnMock invocation) -> {

            if (invocation.getArgument(0).equals(SERVICE_NAME)) {

                Service service = new Service();
                service.setName(SERVICE_NAME);
                service.setPrice(SERVICE_PRICE);
                service.setDuration(SERVICE_DURATION);

                return service;

            } else throw new Exception("The provided service name is invalid");
        });

        lenient().when(customerRepository.findCustomerByEmail(any(String.class))).thenAnswer((InvocationOnMock invocation) -> {

            if (invocation.getArgument(0).equals(CUSTOMER_EMAIL)) {

                Customer customer = new Customer();
                customer.setEmail(CUSTOMER_EMAIL);

                return customer;

            } else throw new Exception("The provided customer email is invalid");
        });

        lenient().when(businessRepository.findBusinessByName(any(String.class))).thenAnswer((InvocationOnMock invocation) -> {

            if (invocation.getArgument(0).equals(BUSINESS_NAME)) {

                Business business = new Business();
                business.setName(BUSINESS_NAME);
                TimeSlot holiday = new TimeSlot();
                holiday.setStartDateTime(HOLIDAY_START);
                holiday.setEndDateTime(HOLIDAY_END);
                List<TimeSlot> holidays = new ArrayList<>();
                holidays.add(holiday);
                business.setHolidays(holidays);

                return business;

            } else throw new Exception("The provided business name is invalid");
        });

        lenient().when(technicianRepository.findTechnicianByEmail(any(String.class))).thenAnswer((InvocationOnMock invocation) -> {

            if (invocation.getArgument(0).equals(TECHNICIAN_EMAIL)) {

                Technician technician = new Technician();
                technician.setEmail(TECHNICIAN_EMAIL);
                TimeSlot appSlot = new TimeSlot();
                appSlot.setStartDateTime(APP_START);
                appSlot.setEndDateTime(APP_END);
                Appointment app = new Appointment();
                app.setTimeSlot(appSlot);
                List<Appointment> apps = new ArrayList<>();
                apps.add(app);
                technician.setAppointments(apps);
                TimeSlot hours = new TimeSlot();
                hours.setStartDateTime(HOURS_START);
                hours.setEndDateTime(HOURS_END);
                TimeSlot hours2 = new TimeSlot();
                hours2.setStartDateTime(HOURS_START2);
                hours2.setEndDateTime(HOURS_END2);
                List<TimeSlot> workHours = new ArrayList<>();
                workHours.add(hours); // Monday
                workHours.add(hours2); // Tuesday
                technician.setTimeslots(workHours);

                return technician;

            } else if (invocation.getArgument(0).equals(TECHNICIAN_EMAIL2)) {

                Technician technician2 = new Technician();
                technician2.setEmail(TECHNICIAN_EMAIL);
                TimeSlot appSlot = new TimeSlot();
                appSlot.setStartDateTime(APP_START);
                appSlot.setEndDateTime(APP_END);
                Appointment app = new Appointment();
                app.setTimeSlot(appSlot);
                List<Appointment> apps = new ArrayList<>();
                apps.add(app);
                technician2.setAppointments(apps);
                TimeSlot hours2 = new TimeSlot();
                hours2.setStartDateTime(HOURS_START2);
                hours2.setEndDateTime(HOURS_END2);
                TimeSlot hours3 = new TimeSlot();
                hours3.setStartDateTime(HOURS_START3);
                hours3.setEndDateTime(HOURS_END3);
                List<TimeSlot> workHours = new ArrayList<>();
                workHours.add(hours2);
                workHours.add(hours3);
                technician2.setTimeslots(workHours);

                return technician2;

            } else throw new Exception("A technician's email is invalid");
        });

        lenient().when(appointmentRepository.save(any(Appointment.class))).thenAnswer((InvocationOnMock invocation) -> invocation.getArgument(0));

    }

    @Test // valid appointment
    public void testCreateAppointment() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment(APP_START_TIME, SERVICE_NAME, TECHNICIAN_EMAIL + ", " + TECHNICIAN_EMAIL2, CUSTOMER_EMAIL, BUSINESS_NAME);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNotNull(appointmentDto);
        assertEquals(APP_START_TIME, appointmentDto.getTimeSlotDto().getStartDateTime().toString());
        assertEquals(SERVICE_NAME, appointmentDto.getServiceDto().getName());
        assertEquals(TECHNICIAN_EMAIL, appointmentDto.getTechnicianDto().getEmail());
        assertEquals(CUSTOMER_EMAIL, appointmentDto.getCustomerDto().getEmail());

    }

    @Test // invalid appointment - no technician with work hours
    public void testCreateAppointmentTechnicianUnavailable() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment(APP_START_TIME, SERVICE_NAME, TECHNICIAN_EMAIL2, CUSTOMER_EMAIL, BUSINESS_NAME);
            fail();
        } catch (Exception e) {
            assertEquals("The appointment cannot be booked", e.getMessage());
        }

        assertNull(appointmentDto);
    }

    @Test // invalid appointment - invalid start time (null timestamp)
    public void testCreateAppointmentInvalidTimestampNull() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment(null, SERVICE_NAME, TECHNICIAN_EMAIL + ", " + TECHNICIAN_EMAIL2, CUSTOMER_EMAIL, BUSINESS_NAME);
            fail();
        } catch (Exception e) {
            assertEquals("The Timestamp is mandatory", e.getMessage());
        }

        assertNull(appointmentDto);
    }

    @Test // invalid appointment - invalid start time (empty timestamp)
    public void testCreateAppointmentInvalidTimestampEmpty() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment("", SERVICE_NAME, TECHNICIAN_EMAIL + ", " + TECHNICIAN_EMAIL2, CUSTOMER_EMAIL, BUSINESS_NAME);
            fail();
        } catch (Exception e) {
            assertEquals("The Timestamp is mandatory", e.getMessage());
        }

        assertNull(appointmentDto);
    }

    @Test // invalid appointment - invalid start time (wrong format for timestamp)
    public void testCreateAppointmentInvalidTimestamp() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment("notATimestamp", SERVICE_NAME, TECHNICIAN_EMAIL + ", " + TECHNICIAN_EMAIL2, CUSTOMER_EMAIL, BUSINESS_NAME);
            fail();
        } catch (Exception e) {
            assertEquals("The provided Timestamp is invalid", e.getMessage());
        }

        assertNull(appointmentDto);
    }

    @Test // invalid appointment - invalid start time (time has passed)
    public void testCreateAppointmentInvalidTimestampInPast() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment(APP_START_TIME2, SERVICE_NAME, TECHNICIAN_EMAIL + ", " + TECHNICIAN_EMAIL2, CUSTOMER_EMAIL, BUSINESS_NAME);
            fail();
        } catch (Exception e) {
            assertEquals("The provided Timestamp is invalid", e.getMessage());
        }

        assertNull(appointmentDto);
    }

    @Test // invalid appointment - invalid start time (overlaps end of technician's hours)
    public void testCreateAppointmentInvalidTimestampEndHours() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment(APP_START_TIME3, SERVICE_NAME, TECHNICIAN_EMAIL + ", " + TECHNICIAN_EMAIL2, CUSTOMER_EMAIL, BUSINESS_NAME);
            fail();
        } catch (Exception e) {
            assertEquals("The appointment cannot be booked", e.getMessage());
        }

        assertNull(appointmentDto);
    }

    @Test // invalid appointment - invalid start time (outside of technician's hours)
    public void testCreateAppointmentInvalidTimestampOutsideHours() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment(APP_START_TIME4, SERVICE_NAME, TECHNICIAN_EMAIL + ", " + TECHNICIAN_EMAIL2, CUSTOMER_EMAIL, BUSINESS_NAME);
            fail();
        } catch (Exception e) {
            assertEquals("The appointment cannot be booked", e.getMessage());
        }

        assertNull(appointmentDto);
    }

    @Test // invalid appointment - invalid start time (during holiday)
    public void testCreateAppointmentInvalidTimestampDuringHoliday() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment(APP_START_TIME5, SERVICE_NAME, TECHNICIAN_EMAIL + ", " + TECHNICIAN_EMAIL2, CUSTOMER_EMAIL, BUSINESS_NAME);
            fail();
        } catch (Exception e) {
            assertEquals("The appointment cannot be booked", e.getMessage());
        }

        assertNull(appointmentDto);
    }

    @Test // invalid appointment - invalid start time (overlaps with another appointment)
    public void testCreateAppointmentInvalidTimestampOverlapAppointment() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment(APP_START_TIME6, SERVICE_NAME, TECHNICIAN_EMAIL + ", " + TECHNICIAN_EMAIL2, CUSTOMER_EMAIL, BUSINESS_NAME);
            fail();
        } catch (Exception e) {
            assertEquals("The appointment cannot be booked", e.getMessage());
        }

        assertNull(appointmentDto);
    }

    @Test // invalid appointment - invalid start time (during another appointment)
    public void testCreateAppointmentInvalidTimestampDuringAppointment() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment(APP_START_TIME7, SERVICE_NAME, TECHNICIAN_EMAIL + ", " + TECHNICIAN_EMAIL2, CUSTOMER_EMAIL, BUSINESS_NAME);
            fail();
        } catch (Exception e) {
            assertEquals("The appointment cannot be booked", e.getMessage());
        }

        assertNull(appointmentDto);
    }

    @Test // invalid appointment - invalid service (null service)
    public void testCreateAppointmentInvalidServiceNull() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment(APP_START_TIME, null, TECHNICIAN_EMAIL + ", " + TECHNICIAN_EMAIL2, CUSTOMER_EMAIL, BUSINESS_NAME);
            fail();
        } catch (Exception e) {
            assertEquals("The service name is mandatory", e.getMessage());
        }

        assertNull(appointmentDto);
    }

    @Test // invalid appointment - invalid service (empty service)
    public void testCreateAppointmentInvalidServiceEmpty() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment(APP_START_TIME, "", TECHNICIAN_EMAIL + ", " + TECHNICIAN_EMAIL2, CUSTOMER_EMAIL, BUSINESS_NAME);
            fail();
        } catch (Exception e) {
            assertEquals("The service name is mandatory", e.getMessage());
        }

        assertNull(appointmentDto);
    }

    @Test // invalid appointment - invalid service
    public void testCreateAppointmentInvalidService() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment(APP_START_TIME, "notAService", TECHNICIAN_EMAIL + ", " + TECHNICIAN_EMAIL2, CUSTOMER_EMAIL, BUSINESS_NAME);
            fail();
        } catch (Exception e) {
            assertEquals("The provided service name is invalid", e.getMessage());
        }

        assertNull(appointmentDto);
    }

    @Test // invalid appointment - invalid technician (null technicians)
    public void testCreateAppointmentInvalidTechniciansNull() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment(APP_START_TIME, SERVICE_NAME, null, CUSTOMER_EMAIL, BUSINESS_NAME);
            fail();
        } catch (Exception e) {
            assertEquals("Technicians are mandatory", e.getMessage());
        }

        assertNull(appointmentDto);
    }

    @Test // invalid appointment - invalid technician (empty technician)
    public void testCreateAppointmentInvalidTechniciansEmpty() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment(APP_START_TIME, SERVICE_NAME, "", CUSTOMER_EMAIL, BUSINESS_NAME);
            fail();
        } catch (Exception e) {
            assertEquals("Technicians are mandatory", e.getMessage());
        }

        assertNull(appointmentDto);
    }

    @Test // invalid appointment - invalid technician
    public void testCreateAppointmentInvalidTechnicians() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment(APP_START_TIME, SERVICE_NAME, "notTechnicians", CUSTOMER_EMAIL, BUSINESS_NAME);
            fail();
        } catch (Exception e) {
            assertEquals("A technician's email is invalid", e.getMessage());
        }

        assertNull(appointmentDto);
    }

    @Test // invalid appointment - invalid customer (null customer)
    public void testCreateAppointmentInvalidCustomerNull() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment(APP_START_TIME, SERVICE_NAME, TECHNICIAN_EMAIL + ", " + TECHNICIAN_EMAIL2, null, BUSINESS_NAME);
            fail();
        } catch (Exception e) {
            assertEquals("The customer is mandatory", e.getMessage());
        }

        assertNull(appointmentDto);
    }

    @Test // invalid appointment - invalid customer (empty customer)
    public void testCreateAppointmentInvalidCustomerEmpty() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment(APP_START_TIME, SERVICE_NAME, TECHNICIAN_EMAIL + ", " + TECHNICIAN_EMAIL2, "", BUSINESS_NAME);
            fail();
        } catch (Exception e) {
            assertEquals("The customer is mandatory", e.getMessage());
        }

        assertNull(appointmentDto);
    }

    @Test // invalid appointment - invalid customer
    public void testCreateAppointmentInvalidCustomer() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment(APP_START_TIME, SERVICE_NAME, TECHNICIAN_EMAIL + ", " + TECHNICIAN_EMAIL2, "notACustomer", BUSINESS_NAME);
            fail();
        } catch (Exception e) {
            assertEquals("The provided customer email is invalid", e.getMessage());
        }

        assertNull(appointmentDto);
    }

    @Test // invalid appointment - invalid business (null business)
    public void testCreateAppointmentInvalidBusinessNull() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment(APP_START_TIME, SERVICE_NAME, TECHNICIAN_EMAIL + ", " + TECHNICIAN_EMAIL2, CUSTOMER_EMAIL, null);
            fail();
        } catch (Exception e) {
            assertEquals("The business is mandatory", e.getMessage());
        }

        assertNull(appointmentDto);
    }

    @Test // invalid appointment - invalid business (empty business)
    public void testCreateAppointmentInvalidBusinessEmpty() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment(APP_START_TIME, SERVICE_NAME, TECHNICIAN_EMAIL + ", " + TECHNICIAN_EMAIL2, CUSTOMER_EMAIL, "");
            fail();
        } catch (Exception e) {
            assertEquals("The business is mandatory", e.getMessage());
        }

        assertNull(appointmentDto);
    }

    @Test // invalid appointment - invalid business
    public void testCreateAppointmentInvalidBusiness() {

        AppointmentDto appointmentDto = null;

        try {
            appointmentDto = appointmentService.createAppointment(APP_START_TIME, SERVICE_NAME, TECHNICIAN_EMAIL + ", " + TECHNICIAN_EMAIL2, CUSTOMER_EMAIL, "notABusiness");
            fail();
        } catch (Exception e) {
            assertEquals("The provided business name is invalid", e.getMessage());
        }

        assertNull(appointmentDto);
    }

}
