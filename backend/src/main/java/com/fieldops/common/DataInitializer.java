package com.fieldops.common;

import com.fieldops.customer.Customer;
import com.fieldops.customer.CustomerRepository;
import com.fieldops.inventory.InventoryItem;
import com.fieldops.inventory.InventoryItemRepository;
import com.fieldops.inventory.InventoryItemStatus;
import com.fieldops.notification.Notification;
import com.fieldops.notification.NotificationRepository;
import com.fieldops.notification.NotificationSeverity;
import com.fieldops.technician.Technician;
import com.fieldops.technician.TechnicianRepository;
import com.fieldops.technician.TechnicianStatus;
import com.fieldops.user.Role;
import com.fieldops.user.User;
import com.fieldops.user.UserRepository;
import com.fieldops.workorder.WorkOrder;
import com.fieldops.workorder.WorkOrderPriority;
import com.fieldops.workorder.WorkOrderRepository;
import com.fieldops.workorder.WorkOrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final TechnicianRepository technicianRepository;
    private final CustomerRepository customerRepository;
    private final WorkOrderRepository workOrderRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           TechnicianRepository technicianRepository,
                           CustomerRepository customerRepository,
                           WorkOrderRepository workOrderRepository,
                           InventoryItemRepository inventoryItemRepository,
                           NotificationRepository notificationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.technicianRepository = technicianRepository;
        this.customerRepository = customerRepository;
        this.workOrderRepository = workOrderRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            log.info("Demo data already seeded. Skipping initialization.");
            return;
        }

        log.info("Starting FieldOps Demo Data Seeding...");

        seedUsers();
        List<Technician> technicians = seedTechnicians();
        List<Customer> customers = seedCustomers();
        seedWorkOrders(technicians, customers);
        seedInventory();
        seedNotifications();

        log.info("FieldOps Demo Data Seeding completed successfully!");
    }

    private void seedUsers() {
        String defaultPassword = passwordEncoder.encode("FieldOps@2026!");

        userRepository.save(User.builder()
                .email("admin@fieldops.demo")
                .passwordHash(defaultPassword)
                .fullName("Sarah Jenkins")
                .role(Role.ADMIN)
                .phone("+91 98100 12345")
                .organization("FieldOps Global Operations")
                .avatarUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150")
                .active(true)
                .build());

        userRepository.save(User.builder()
                .email("dispatcher@fieldops.demo")
                .passwordHash(defaultPassword)
                .fullName("Marcus Vance")
                .role(Role.DISPATCHER)
                .phone("+91 98100 23456")
                .organization("Northern Dispatch Command")
                .avatarUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150")
                .active(true)
                .build());

        userRepository.save(User.builder()
                .email("technician@fieldops.demo")
                .passwordHash(defaultPassword)
                .fullName("Rahul Sharma")
                .role(Role.TECHNICIAN)
                .phone("+91 98100 34567")
                .organization("Field Engineering Group")
                .avatarUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150")
                .active(true)
                .build());

        userRepository.save(User.builder()
                .email("customer@fieldops.demo")
                .passwordHash(defaultPassword)
                .fullName("Pooja Singhania")
                .role(Role.CUSTOMER)
                .phone("+91 98100 45678")
                .organization("Vertex Logistics Park")
                .avatarUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150")
                .active(true)
                .build());
    }

    private List<Technician> seedTechnicians() {
        List<Technician> list = new ArrayList<>();

        Object[][] techData = {
                {"Rahul Sharma", "rahul.s@fieldops.demo", "+91 98110 01101", TechnicianStatus.BUSY, "Gurgaon", "DLF Phase 4, Gurgaon", 28.4601, 77.0850, 4.9, 142, 98.4, 2, 88.0, Set.of("HVAC Systems", "Inverter Drive", "BMS Systems")},
                {"Priya Verma", "priya.v@fieldops.demo", "+91 98110 01102", TechnicianStatus.AVAILABLE, "Delhi", "Connaught Place, New Delhi", 28.6315, 77.2167, 4.85, 126, 97.2, 0, 45.0, Set.of("Fiber Optics", "Network Switches", "OTDR Testing")},
                {"Amit Patel", "amit.p@fieldops.demo", "+91 98110 01103", TechnicianStatus.ON_ROUTE, "Gurgaon", "Cyber City, DLF Phase 2", 28.4905, 77.0900, 4.75, 98, 94.5, 1, 72.0, Set.of("Electrical Panels", "High Voltage Switchgear", "Thermal Imaging")},
                {"Ananya Singh", "ananya.s@fieldops.demo", "+91 98110 01104", TechnicianStatus.AVAILABLE, "Noida", "Sector 62, Electronic City", 28.6258, 77.3730, 4.92, 115, 99.1, 0, 30.0, Set.of("Solar Inverters", "Grid Synchronizer", "Battery Energy Storage")},
                {"Vikram Sethi", "vikram.s@fieldops.demo", "+91 98110 01105", TechnicianStatus.BUSY, "Noida", "Sector 18, Noida", 28.5700, 77.3200, 4.65, 84, 93.0, 1, 85.0, Set.of("Industrial Automation", "PLC Programming", "SCADA")},
                {"Rohan Kapoor", "rohan.k@fieldops.demo", "+91 98110 01106", TechnicianStatus.AVAILABLE, "Delhi", "Okhla Industrial Area Ph-3", 28.5355, 77.2725, 4.80, 102, 96.0, 0, 50.0, Set.of("Robotics", "Servo Calibration", "Industrial Hydraulics")},
                {"Deepika Nair", "deepika.n@fieldops.demo", "+91 98110 01107", TechnicianStatus.ON_ROUTE, "Delhi", "Aerocity Worldmark 2", 28.5520, 77.1215, 4.88, 110, 97.8, 1, 78.0, Set.of("Chiller Plants", "Refrigerant Reclaim", "BMS Systems")},
                {"Suresh Menon", "suresh.m@fieldops.demo", "+91 98110 01108", TechnicianStatus.AVAILABLE, "Gurgaon", "Sector 48, Sohna Road", 28.4200, 77.0350, 4.70, 78, 92.5, 0, 40.0, Set.of("Fiber Optics", "Structured Cabling", "GPON Networks")},
                {"Neha Kulkarni", "neha.k@fieldops.demo", "+91 98110 01109", TechnicianStatus.BUSY, "Jaipur", "Sitapura Industrial Area", 26.7800, 75.8300, 4.95, 134, 99.5, 2, 92.0, Set.of("Solar Inverters", "Substation Relay", "Power Factor Regulators")},
                {"Manish Joshi", "manish.j@fieldops.demo", "+91 98110 01110", TechnicianStatus.AVAILABLE, "Jaipur", "Malviya Nagar, Jaipur", 26.8550, 75.8150, 4.68, 65, 91.0, 0, 25.0, Set.of("Commercial HVAC", "Air Handling Units", "VAV Controllers")},
                {"Harpreet Kaur", "harpreet.k@fieldops.demo", "+91 98110 01111", TechnicianStatus.AVAILABLE, "Chandigarh", "IT Park, Kishangarh", 30.7250, 76.8400, 4.90, 118, 98.0, 0, 35.0, Set.of("Data Center Cooling", "CRAC Units", "Dual-Cord Redundancy")},
                {"Gaurav Malhotra", "gaurav.m@fieldops.demo", "+91 98110 01112", TechnicianStatus.ON_ROUTE, "Chandigarh", "Industrial Area Phase 1", 30.7050, 76.8000, 4.72, 89, 94.2, 1, 70.0, Set.of("Diesel Gensets", "Auto Transfer Switches", "UPS Systems")},
                {"Arjun Reddy", "arjun.r@fieldops.demo", "+91 98110 01113", TechnicianStatus.AVAILABLE, "Delhi", "Netaji Subhash Place, Pitampura", 28.6940, 77.1510, 4.82, 94, 96.5, 0, 48.0, Set.of("Fire Suppression", "Gas Flooding Sensors", "Smoke Damper Actuators")},
                {"Kavita Gupta", "kavita.g@fieldops.demo", "+91 98110 01114", TechnicianStatus.BUSY, "Gurgaon", "Manesar Industrial Sector 8", 28.3600, 76.9200, 4.78, 107, 95.8, 1, 82.0, Set.of("Variable Frequency Drives", "Motor Rewinding", "Vibration Analysis")},
                {"Karan Rawat", "karan.r@fieldops.demo", "+91 98110 01115", TechnicianStatus.AVAILABLE, "Noida", "Sector 135 Express Trade Towers", 28.5000, 77.4000, 4.86, 112, 97.0, 0, 52.0, Set.of("Access Control", "CCTV IP Matrix", "Biometric Terminal")},
                {"Swati Sen", "swati.s@fieldops.demo", "+91 98110 01116", TechnicianStatus.AVAILABLE, "Delhi", "Nehru Place IT Market", 28.5490, 77.2520, 4.91, 129, 98.7, 0, 42.0, Set.of("Optical Splice", "Core Router Linecard", "WDM Transponders")},
                {"Tarun Deshmukh", "tarun.d@fieldops.demo", "+91 98110 01117", TechnicianStatus.ON_ROUTE, "Gurgaon", "Golf Course Ext Road, Sector 58", 28.4050, 77.1020, 4.60, 58, 90.5, 1, 68.0, Set.of("Chilled Water Piping", "Hydronic Balancing", "Variable Pumping")},
                {"Meera Rao", "meera.r@fieldops.demo", "+91 98110 01118", TechnicianStatus.AVAILABLE, "Jaipur", "Mansarovar Sector 5", 26.8650, 75.7650, 4.84, 91, 96.8, 0, 38.0, Set.of("IoT Sensors", "LoRaWAN Gateways", "Smart Metering")},
                {"Jaswinder Singh", "jaswinder.s@fieldops.demo", "+91 98110 01119", TechnicianStatus.BUSY, "Chandigarh", "Mohali Phase 8B Industrial Focal Point", 30.7000, 76.7200, 4.77, 103, 95.1, 1, 80.0, Set.of("Industrial Chillers", "Screw Compressor Overhaul", "Ammonia Detection")},
                {"Sanjay Bhat", "sanjay.b@fieldops.demo", "+91 98110 01120", TechnicianStatus.AVAILABLE, "Noida", "Greater Noida Knowledge Park III", 28.4700, 77.5000, 4.88, 120, 97.9, 0, 45.0, Set.of("Solar PV Modules", "Combiner Box", "Utility Interconnect")},
                {"Pooja Chauhan", "pooja.c@fieldops.demo", "+91 98110 01121", TechnicianStatus.AVAILABLE, "Delhi", "Patparganj Industrial Area", 28.6300, 77.3000, 4.79, 87, 94.8, 0, 50.0, Set.of("Switchboards", "Arc Flash Protection", "Earth Resistance Testing")}
        };

        for (Object[] row : techData) {
            Technician t = Technician.builder()
                    .name((String) row[0])
                    .email((String) row[1])
                    .phone((String) row[2])
                    .status((TechnicianStatus) row[3])
                    .city((String) row[4])
                    .address((String) row[5])
                    .latitude((Double) row[6])
                    .longitude((Double) row[7])
                    .rating((Double) row[8])
                    .completedJobs((Integer) row[9])
                    .slaSuccessRate((Double) row[10])
                    .activeJobsCount((Integer) row[11])
                    .utilizationRate((Double) row[12])
                    .skills((Set<String>) row[13])
                    .avatarUrl("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150")
                    .build();
            list.add(technicianRepository.save(t));
        }

        return list;
    }

    private List<Customer> seedCustomers() {
        List<Customer> list = new ArrayList<>();

        Object[][] custData = {
                {"Vertex Logistics Park", "Vertex Global Supply Chain Ltd", "dispatch@vertexlogistics.in", "+91 124 4567801", "Gurgaon", "Tauru Road, Bilaspur Industrial Cluster", 28.3200, 76.8800, 4, 38, 4.92, "High priority cold-storage facility with 24/7 SLA requirement."},
                {"DLF CyberPark Facilities", "DLF Assets Ltd", "infrastructure@dlf.in", "+91 124 4890000", "Gurgaon", "Tower A, 4th Floor, DLF CyberPark", 28.4980, 77.0910, 3, 52, 4.88, "Tier-1 commercial office hub housing Fortune 500 tech companies."},
                {"AeroCity International Hub", "Delhi Airport Logistics Co", "aircargo.ops@delhiairport.com", "+91 11 4987000", "Delhi", "Asset Area 4, Hospitality District, Aerocity", 28.5550, 77.1230, 2, 44, 4.95, "Mission-critical air-cargo refrigeration and power distribution."},
                {"Noida Tech One Campus", "HCL Tech Real Estate Operations", "campus.ops@hcl.com", "+91 120 4569000", "Noida", "Sector 126, Noida-Greater Noida Expressway", 28.5400, 77.3400, 3, 61, 4.85, "Hyperscale IT campus with redundant generator backup grids."},
                {"Fortis Healthcare North", "Fortis Hospital Trust", "biomedical.ops@fortishealthcare.com", "+91 124 7160000", "Gurgaon", "Sector 44, Opposite Huda City Metro", 28.4570, 77.0720, 2, 49, 4.98, "Zero tolerance medical facility. Immediate response priority level 1."},
                {"Radisson Blu Tech Center", "Carlson Rezidor Hospitality", "chief.engineer@radissonblu.com", "+91 120 4300000", "Noida", "Plot 2, Sector 18, Noida", 28.5680, 77.3220, 1, 29, 4.80, "Central chiller water loops and building management controllers."},
                {"Titan Watch Assembly Line", "Titan Company Limited", "plant.eng@titan.co.in", "+91 141 2770500", "Jaipur", "RIICO Industrial Area, Sitapura Ph-3", 26.7750, 75.8350, 2, 33, 4.90, "Precision robotics and dust-free clean room air conditioning."},
                {"Infosys Mohali Development", "Infosys Limited", "facilities.mohali@infosys.com", "+91 172 5040000", "Chandigarh", "Plot 1, Rajiv Gandhi Technology Park, Kishangarh", 30.7280, 76.8420, 2, 47, 4.92, "High-density data center and fiber cross-connect facilities."},
                {"Schneider Smart Factory", "Schneider Electric India", "automation.lab@se.com", "+91 124 6781000", "Gurgaon", "Sector 8, IMT Manesar", 28.3650, 76.9250, 1, 26, 4.87, "Industrial IoT and switchgear testing environment."},
                {"Adani Solar Farm Node 4", "Adani Green Energy Ltd", "solar.scada@adani.com", "+91 141 5562000", "Jaipur", "Phagi Solar Feeder Road", 26.6000, 75.6000, 3, 31, 4.79, "33kV feeder substation and grid tied inverters."},
                {"Apollo Cradle Maternal Center", "Apollo Hospitals Group", "maint.delhi@apollo.in", "+91 11 2682555", "Delhi", "Nehru Enclave, Kalkaji", 28.5420, 77.2500, 1, 19, 4.94, "UPS backup and HVAC pathogen filtration."},
                {"Hero MotoCorp R&D Centre", "Hero MotoCorp Ltd", "rnd.facilities@heromotocorp.com", "+91 141 7100000", "Jaipur", "Kukas Industrial Area", 27.0500, 75.9200, 2, 41, 4.86, "Automotive dyno test benches and environmental simulation chambers."},
                {"ITC Maurya Luxury Towers", "ITC Hotels Limited", "engineering.maurya@itchotels.in", "+91 11 2611223", "Delhi", "Diplomatic Enclave, Chanakyapuri", 28.5950, 77.1720, 1, 35, 4.91, "Chilled water coils and energy recovery ventilators."},
                {"QuarkCity Tech Hub", "Quark Media Systems India", "admin@quarkcity.com", "+91 172 2299000", "Chandigarh", "A-45, Industrial Focal Point Phase VIII-B, Mohali", 30.7020, 76.7180, 2, 22, 4.83, "High availability networking racks and optical patch panels."},
                {"Paytm Headquarters Tower", "One97 Communications Ltd", "infra@paytm.com", "+91 120 4770770", "Noida", "B-121, Sector 5, Noida", 28.5880, 77.3150, 2, 39, 4.89, "Server rooms and enterprise battery banks."},
                {"Nestle India Logistics", "Nestle India Limited", "coldchain@nestle.com", "+91 124 2345000", "Gurgaon", "Pataudi Road Industrial Zone", 28.4100, 76.9100, 1, 27, 4.85, "Continuous temperature monitored chocolate storage lines."},
                {"Max Super Speciality Hospital", "Max Healthcare Institute", "biomed.patparganj@maxhealthcare.com", "+91 11 4303333", "Delhi", "108 A, I.P. Extension, Patparganj", 28.6290, 77.3020, 1, 34, 4.96, "Oxygen generator plant and vacuum pump redundancy."},
                {"Wipro Digital Hub", "Wipro Limited", "facilities.noida@wipro.com", "+91 120 4848000", "Noida", "Plot No. 480-481, Knowledge Park III, Greater Noida", 28.4720, 77.4980, 2, 40, 4.88, "Smart lighting and intelligent BMS automation."},
                {"Maruti Suzuki Factory Gate 2", "Maruti Suzuki India Ltd", "plant2.maintenance@maruti.co.in", "+91 124 4392000", "Gurgaon", "Old Palam Gurgaon Road", 28.5100, 77.0600, 3, 58, 4.90, "High tonnage presses and paint shop exhaust ducting."},
                {"Jaipur Rugs HQ & Studios", "Jaipur Rugs Pvt Ltd", "facilities@jaipurrugs.com", "+91 141 3987000", "Jaipur", "Mansarovar Industrial Area", 26.8620, 75.7600, 1, 18, 4.84, "Humidification systems and solar rooftop array."},
                {"Godrej Properties Horizon", "Godrej Properties Ltd", "facility.gurgaon@godrejproperties.com", "+91 124 4561000", "Gurgaon", "Sector 43, Golf Course Road", 28.4550, 77.0980, 2, 31, 4.87, "Elevator motor drives and high pressure booster pumps."},
                {"Samsung R&D Institute", "Samsung India Electronics", "campus.tech@samsung.com", "+91 120 3800000", "Noida", "Sector 142 Noida Expressway", 28.5120, 77.4100, 2, 45, 4.93, "Semiconductor test lab air purification and RF shielded rooms."},
                {"Chitkara Innovation Incubator", "Chitkara Educational Trust", "innovation@chitkara.edu.in", "+91 172 4003000", "Chandigarh", "Sector 26 Industrial Area, Chandigarh", 30.7180, 76.8100, 1, 15, 4.82, "Prototyping CNC machines and 3D print exhaust."},
                {"Bosch Chassis Systems", "Bosch Limited", "plant.maintenance@in.bosch.com", "+91 141 2771000", "Jaipur", "Sitapura Phase 2, Jaipur", 26.7820, 75.8280, 2, 28, 4.91, "Brake test rigs and hydraulic valve blocks."},
                {"Bharti Airtel Network NOC", "Bharti Airtel Limited", "noc.delhi@airtel.com", "+91 11 4666610", "Gurgaon", "Nelson Mandela Marg & Gurgaon NOC", 28.5020, 77.0880, 3, 50, 4.94, "Tier-4 telecom NOC with quad-redundant power feeds."},
                {"Tata Consultancy Services", "Tata Consultancy Services Ltd", "facility.noida@tcs.com", "+91 120 6790000", "Noida", "Sector 62, Block A", 28.6270, 77.3680, 2, 42, 4.89, "Server hall chilling towers and clean gas fire suppression."},
                {"Punjab Cricket Association", "PCA Stadium Mohali", "engineering@pcacricket.com", "+91 172 2232000", "Chandigarh", "Sector 63, SAS Nagar Mohali", 30.6900, 76.7350, 1, 14, 4.85, "Floodlight 2MVA transformer sub-station and turf irrigation."},
                {"Larsen & Toubro Heavy Fab", "Larsen & Toubro Ltd", "heavyeng.delhi@larsentoubro.com", "+91 120 4002000", "Delhi", "Mathura Road, Mohan Cooperative", 28.5100, 77.2950, 2, 37, 4.88, "Overhead gantry crane drives and MIG welding gas distribution."},
                {"Amber Enterprises Electronics", "Amber Enterprises India", "plant.cooling@ambergroupindia.com", "+91 124 4568900", "Gurgaon", "Sector 5, IMT Manesar", 28.3580, 76.9150, 2, 29, 4.86, "HVAC PCB assembly and environmental burn-in ovens."},
                {"Clarks Amer Heritage Hotel", "UP Hotels Limited", "maintenance@hotelclarks.com", "+91 141 2550616", "Jaipur", "Jawaharlal Nehru Marg, Jaipur", 26.8500, 75.8050, 1, 23, 4.87, "Historic heritage property electrical restoration and backup genset."}
        };

        for (Object[] row : custData) {
            Customer c = Customer.builder()
                    .name((String) row[0])
                    .company((String) row[1])
                    .email((String) row[2])
                    .phone((String) row[3])
                    .city((String) row[4])
                    .address((String) row[5])
                    .latitude((Double) row[6])
                    .longitude((Double) row[7])
                    .openJobsCount((Integer) row[8])
                    .completedJobsCount((Integer) row[9])
                    .satisfactionScore((Double) row[10])
                    .notes((String) row[11])
                    .lastServiceDate(LocalDateTime.now().minusDays((int) (Math.random() * 15) + 1))
                    .build();
            list.add(customerRepository.save(c));
        }

        return list;
    }

    private void seedWorkOrders(List<Technician> technicians, List<Customer> customers) {
        // 1. Critical Highlight WO-1042
        Customer custFortis = customers.get(4);
        Technician techRahul = technicians.get(0);
        workOrderRepository.save(WorkOrder.builder()
                .workOrderNumber("WO-1042")
                .title("Critical HVAC Inverter Failure - ICU Chiller Loop")
                .description("Primary inverter drive tripped with Overcurrent Fault code E09. Hospital ICU ambient temperature creeping upward. Redundant pump failing to start on auto-transfer.")
                .priority(WorkOrderPriority.CRITICAL)
                .status(WorkOrderStatus.IN_PROGRESS)
                .customer(custFortis)
                .technician(techRahul)
                .locationName("Fortis Hospital Central Wing")
                .city("Gurgaon")
                .address("Sector 44, Gurgaon")
                .latitude(28.4570)
                .longitude(77.0720)
                .requiredSkills(new HashSet<>(Set.of("HVAC Systems", "Inverter Drive")))
                .scheduledStartTime(LocalDateTime.now().minusMinutes(40))
                .scheduledEndTime(LocalDateTime.now().plusMinutes(60))
                .slaDeadline(LocalDateTime.now().plusMinutes(25))
                .estimatedDurationHours(1.5)
                .slaBreachProbability(82.0)
                .technicianEtaMinutes(21)
                .distanceKm(4.2)
                .serviceNotes("Technician Rahul is currently en route with replacement VFD cooling fan assembly. Bypass contactor engaged manually as temporary stopgap.")
                .aiInsight("Current assignment has an estimated 82% probability of SLA breach due to peak evening traffic on NH-48 and high customer backlog.")
                .build());

        // 2. High Risk Highlight WO-1047
        Customer custVertex = customers.get(0);
        Technician techAmit = technicians.get(2);
        workOrderRepository.save(WorkOrder.builder()
                .workOrderNumber("WO-1047")
                .title("Cold Storage Main Switchgear Thermal Anomaly")
                .description("Infrared scanning detected 89°C hotspot on Busbar B phase clamp. High risk of bus melt down risking 45 tons of perishable pharma cargo.")
                .priority(WorkOrderPriority.HIGH)
                .status(WorkOrderStatus.ASSIGNED)
                .customer(custVertex)
                .technician(techAmit)
                .locationName("Vertex Logistics Zone B")
                .city("Gurgaon")
                .address("Tauru Road, Bilaspur")
                .latitude(28.3200)
                .longitude(76.8800)
                .requiredSkills(new HashSet<>(Set.of("Electrical Panels", "Thermal Imaging")))
                .scheduledStartTime(LocalDateTime.now().minusMinutes(10))
                .scheduledEndTime(LocalDateTime.now().plusHours(2))
                .slaDeadline(LocalDateTime.now().plusMinutes(45))
                .estimatedDurationHours(2.0)
                .slaBreachProbability(67.0)
                .technicianEtaMinutes(34)
                .distanceKm(12.8)
                .serviceNotes("Special torque wrench calibrated for 55 Nm and deoxIT compound loaded on truck.")
                .aiInsight("Current assignment has an estimated 67% probability of SLA breach. Recommend dispatching backup assistance if arrival exceeds 30 minutes.")
                .build());

        // 3. Highlight WO-1039 (Completed)
        Customer custAirtel = customers.get(24);
        Technician techPriya = technicians.get(1);
        workOrderRepository.save(WorkOrder.builder()
                .workOrderNumber("WO-1039")
                .title("Fiber Optic Splice Verification - Core Trunk 10G")
                .description("Repair physical fiber cut caused by road excavation. Splice ribbon fibers and verify insertion loss under 0.05dB using OTDR.")
                .priority(WorkOrderPriority.HIGH)
                .status(WorkOrderStatus.COMPLETED)
                .customer(custAirtel)
                .technician(techPriya)
                .locationName("Cyber Hub Optical Node 3")
                .city("Gurgaon")
                .address("DLF Cyber City")
                .latitude(28.4905)
                .longitude(77.0900)
                .requiredSkills(new HashSet<>(Set.of("Fiber Optics", "OTDR Testing")))
                .scheduledStartTime(LocalDateTime.now().minusHours(3))
                .scheduledEndTime(LocalDateTime.now().minusHours(1))
                .slaDeadline(LocalDateTime.now().minusMinutes(30))
                .completedAt(LocalDateTime.now().minusMinutes(45))
                .estimatedDurationHours(2.0)
                .slaBreachProbability(12.0)
                .technicianEtaMinutes(0)
                .distanceKm(0.0)
                .serviceNotes("Splice sealed in IP68 enclosure. Loss test passed at 0.02dB. Service verified operational with NOC.")
                .aiInsight("Completed within SLA target window with exceptional optical quality score.")
                .build());

        // 4. Highlight WO-1024 (Scheduled / In Progress)
        Customer custDLF = customers.get(1);
        Technician techDeepika = technicians.get(6);
        workOrderRepository.save(WorkOrder.builder()
                .workOrderNumber("WO-1024")
                .title("Central Chiller Compressor Vibration Alert")
                .description("Vibration sensors reporting 4.8 mm/s RMS on York Centrifugal Chiller unit 2. Bearing inspection required before full shift startup.")
                .priority(WorkOrderPriority.MEDIUM)
                .status(WorkOrderStatus.IN_PROGRESS)
                .customer(custDLF)
                .technician(techDeepika)
                .locationName("DLF CyberPark Basement Plant")
                .city("Gurgaon")
                .address("Tower A CyberPark")
                .latitude(28.4980)
                .longitude(77.0910)
                .requiredSkills(new HashSet<>(Set.of("Chiller Plants", "BMS Systems")))
                .scheduledStartTime(LocalDateTime.now().minusHours(1))
                .scheduledEndTime(LocalDateTime.now().plusHours(2))
                .slaDeadline(LocalDateTime.now().plusHours(3))
                .estimatedDurationHours(3.0)
                .slaBreachProbability(24.0)
                .technicianEtaMinutes(10)
                .distanceKm(2.1)
                .serviceNotes("Deepika onsite. Laser alignment check underway.")
                .aiInsight("Vibration signature indicates minor misbalance; low risk of catastrophic stall.")
                .build());

        // 5. Highlight WO-1031 (Awaiting Verification)
        Customer custHCL = customers.get(3);
        Technician techAnanya = technicians.get(3);
        workOrderRepository.save(WorkOrder.builder()
                .workOrderNumber("WO-1031")
                .title("500kW Solar Inverter Synchronization Failure")
                .description("Solar array disconnecting from grid during irradiance transition spikes. Anti-islanding relay threshold requires recalibration.")
                .priority(WorkOrderPriority.HIGH)
                .status(WorkOrderStatus.AWAITING_VERIFICATION)
                .customer(custHCL)
                .technician(techAnanya)
                .locationName("HCL Rooftop Array Block C")
                .city("Noida")
                .address("Sector 126, Noida")
                .latitude(28.5400)
                .longitude(77.3400)
                .requiredSkills(new HashSet<>(Set.of("Solar Inverters", "Grid Synchronizer")))
                .scheduledStartTime(LocalDateTime.now().minusHours(2))
                .scheduledEndTime(LocalDateTime.now().minusMinutes(20))
                .slaDeadline(LocalDateTime.now().plusHours(1))
                .estimatedDurationHours(2.0)
                .slaBreachProbability(35.0)
                .technicianEtaMinutes(0)
                .distanceKm(0.0)
                .serviceNotes("Firmware updated to v4.81. Grid synch test completed 3 consecutive lock cycles. Awaiting customer plant manager sign-off.")
                .aiInsight("Operational verification awaiting client digital signature.")
                .build());

        String[] titles = {
                "Substation 11kV Vacuum Circuit Breaker Tripping",
                "Cleanroom Air Handling Unit HEPA Differential Pressure Alert",
                "Data Center Row 4 CRAC Dual Refrigerant Leak Detection",
                "Emergency Diesel Genset 750kVA Fuel Governor Stiction",
                "SCADA Modbus RTU Communication Timeout on PLC Node 12",
                "Automated Guided Vehicle Charging Dock Contact Resistance High",
                "Fire Sprinkler Flow Sensor Intermittent False Alarm",
                "Chilled Water Secondary Pump Mechanical Seal Leakage",
                "Elevator Machine Room Variable Frequency Drive Overheating",
                "Main Distribution Board B phase Busbar Insulator Crack",
                "Warehouse Roof High-Bay LED Circuit Breaker Nuissance Trip",
                "Commercial RO Water Plant High Pressure Pump Cavitation",
                "Biometric Access Turnstile Motor Driver Board Failure",
                "Fiber Distribution Panel Cassette 4 Dirty Connector Clean",
                "Boiler Flue Gas Oxygen Sensor Calibration Drift",
                "Precision Air Conditioner Humidifier Cylinder Scale Accumulation",
                "Air Handling Unit Belt Slippage & RPM Deficit",
                "Server Room Pre-Action Sprinkler Solenoid Valve Test",
                "3-Phase Motor Soft Starter Thyristor Short Circuit",
                "Cooling Tower Induced Draft Fan Gearbox Oil Degradation"
        };

        WorkOrderPriority[] priorities = {WorkOrderPriority.LOW, WorkOrderPriority.MEDIUM, WorkOrderPriority.HIGH, WorkOrderPriority.CRITICAL};
        WorkOrderStatus[] statuses = {WorkOrderStatus.NEW, WorkOrderStatus.ASSIGNED, WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.AWAITING_VERIFICATION, WorkOrderStatus.COMPLETED};

        Random rand = new Random(42);

        for (int i = 1; i <= 48; i++) {
            int woNumber = 1050 + i;
            Customer cust = customers.get(rand.nextInt(customers.size()));
            Technician tech = (rand.nextBoolean() || rand.nextBoolean()) ? technicians.get(rand.nextInt(technicians.size())) : null;
            WorkOrderStatus status = (tech == null) ? WorkOrderStatus.NEW : statuses[rand.nextInt(statuses.length)];
            WorkOrderPriority priority = priorities[rand.nextInt(priorities.length)];

            double breachProb = (priority == WorkOrderPriority.CRITICAL) ? (55.0 + rand.nextDouble() * 35.0) :
                    (priority == WorkOrderPriority.HIGH) ? (35.0 + rand.nextDouble() * 35.0) :
                            (rand.nextDouble() * 30.0);
            breachProb = Math.round(breachProb * 10.0) / 10.0;

            LocalDateTime deadline = LocalDateTime.now().plusHours(rand.nextInt(48) + 1);
            LocalDateTime completedAt = (status == WorkOrderStatus.COMPLETED) ? LocalDateTime.now().minusHours(rand.nextInt(24)) : null;

            workOrderRepository.save(WorkOrder.builder()
                    .workOrderNumber("WO-" + woNumber)
                    .title(titles[rand.nextInt(titles.length)] + " (" + cust.getCity() + ")")
                    .description("Routine diagnostic and corrective field ticket dispatched to ensure SLA compliance at " + cust.getCompany() + ". Inspect hardware tolerances, verify telemetry, and document serial numbers.")
                    .priority(priority)
                    .status(status)
                    .customer(cust)
                    .technician(tech)
                    .locationName(cust.getName() + " - Plant " + ((i % 3) + 1))
                    .city(cust.getCity())
                    .address(cust.getAddress())
                    .latitude(cust.getLatitude() + (rand.nextDouble() - 0.5) * 0.04)
                    .longitude(cust.getLongitude() + (rand.nextDouble() - 0.5) * 0.04)
                    .requiredSkills(tech != null ? new HashSet<>(tech.getSkills()) : new HashSet<>(Set.of("Industrial Automation", "Electrical Panels")))
                    .scheduledStartTime(LocalDateTime.now().plusHours(rand.nextInt(12) - 6))
                    .scheduledEndTime(LocalDateTime.now().plusHours(rand.nextInt(12) + 2))
                    .slaDeadline(deadline)
                    .completedAt(completedAt)
                    .estimatedDurationHours(1.0 + (rand.nextInt(5) * 0.5))
                    .slaBreachProbability(breachProb)
                    .technicianEtaMinutes(tech != null ? (15 + rand.nextInt(45)) : null)
                    .distanceKm(tech != null ? (2.0 + Math.round(rand.nextDouble() * 150.0) / 10.0) : null)
                    .serviceNotes("Field ticket logged by monitoring agent. Status checked via SCADA webhook.")
                    .aiInsight("Model prediction: confidence level " + (85 + rand.nextInt(14)) + "% for timely resolution.")
                    .build());
        }
    }

    private void seedInventory() {
        Object[][] items = {
                {"10Gbps SFP+ Optical Transceiver LR 1310nm", "NET-SFP-10G-LR", "Networking & Optics", 8, 4, 15, 120.0, "units", "Shelf A-12, Delhi Hub"},
                {"Schneider 63A 3-Phase MCB Type C 10kA", "ELEC-MCB-63A-3P", "Electrical Switchgear", 42, 6, 20, 45.0, "units", "Bin E-04, Gurgaon Hub"},
                {"Danfoss VLT 7.5kW Variable Frequency Drive", "DRV-VFD-DANF-75", "Drives & Automation", 4, 2, 5, 1450.0, "units", "Pallet D-02, Gurgaon Hub"},
                {"Carrier Chiller Electronic Expansion Valve 42mm", "HVAC-EEV-42MM", "HVAC Valves & Pumps", 14, 3, 10, 320.0, "units", "Bin H-11, Delhi Hub"},
                {"Siemens S7-1200 Compact CPU 1214C DC/DC/DC", "PLC-SIEM-1214C", "Industrial Controllers", 9, 1, 10, 680.0, "units", "Cabinet C-01, Noida Hub"},
                {"Omron Photoelectric Through-Beam Sensor 20m", "SENS-OMR-E3Z-T61", "Sensors & Detectors", 35, 5, 15, 62.0, "units", "Bin S-08, Noida Hub"},
                {"Fluke 1000V Insulated Field Tool Kit 11-Piece", "TOOL-FLUK-1000V", "Technician Tools", 18, 0, 10, 390.0, "sets", "Locker T-01, Central"},
                {"R410A Eco Refrigerant Canister 11.3kg", "GAS-R410A-11KG", "Chemicals & Gases", 6, 4, 12, 175.0, "cylinders", "Hazmat Bay H-03, Gurgaon"},
                {"Cat6A Shielded S/FTP 500MHz Cable Drum 305m", "CAB-CAT6A-305M", "Cabling & Passive", 22, 4, 10, 210.0, "drums", "Rack R-05, Delhi Hub"},
                {"SunGrow 100kW Solar Inverter DC Surge Module", "SOL-SG-100K-SPD", "Solar Components", 11, 2, 8, 290.0, "units", "Shelf B-09, Jaipur Hub"},
                {"ABB 100A Air Circuit Breaker Shunt Trip Coil", "ELEC-ABB-100A-ST", "Electrical Switchgear", 7, 1, 10, 240.0, "units", "Bin E-15, Chandigarh Hub"},
                {"Grundfos Secondary Inline Pump Mechanical Seal 32mm", "PMP-GRUND-SEAL-32", "HVAC Valves & Pumps", 19, 5, 12, 140.0, "units", "Bin P-03, Gurgaon Hub"},
                {"APC Smart-UPS 5kVA Replacement Battery Cartridge", "PWR-APC-RBC55", "Power & Batteries", 12, 2, 8, 480.0, "packs", "Shelf B-04, Delhi Hub"},
                {"Belimo 24V Spring Return Damper Actuator 20Nm", "ACT-BEL-24V-20NM", "Building Automation", 16, 3, 10, 260.0, "units", "Bin H-07, Noida Hub"},
                {"Fujikura 90S+ Core Alignment Fusion Splicer Electrode", "FIB-FUJ-EL-90S", "Networking & Optics", 25, 2, 10, 95.0, "pairs", "Cabinet F-02, Delhi Hub"},
                {"Honeywell Carbon Monoxide & Combustible Gas Detector", "SENS-HON-CO-GAS", "Sensors & Detectors", 28, 4, 15, 185.0, "units", "Bin S-12, Gurgaon Hub"},
                {"Yaskawa 15kW Servo Motor Encoder Cable 10m", "SRV-YAS-ENC-10M", "Drives & Automation", 15, 2, 8, 130.0, "units", "Rack M-02, Jaipur Hub"},
                {"Cooper Bussmann 200A High Speed Semiconductor Fuse", "FUSE-BUSS-200A-HS", "Electrical Switchgear", 52, 10, 30, 48.0, "units", "Bin F-01, Delhi Hub"},
                {"Daikin VRV Inverter PC Board Assembly", "HVAC-DAIK-VRV-PCB", "HVAC Spares", 3, 2, 6, 850.0, "units", "Shelf H-02, Gurgaon Hub"},
                {"SMA Sunny Tripower Communication Interface Card", "SOL-SMA-COM-CARD", "Solar Components", 8, 1, 6, 310.0, "units", "Shelf B-11, Jaipur Hub"},
                {"Cummins QSK19 Engine Fuel Injector Solenoid", "GEN-CUMM-INJ-SOL", "Generator Spares", 6, 2, 8, 620.0, "units", "Pallet G-01, Chandigarh Hub"},
                {"Yokogawa Differential Pressure Transmitter 4-20mA", "TX-YOKO-DP-420", "Industrial Controllers", 10, 1, 8, 920.0, "units", "Cabinet C-04, Noida Hub"},
                {"Eaton 400V 630A Moulded Case Circuit Breaker", "ELEC-EAT-MCCB-630", "Electrical Switchgear", 5, 1, 5, 1100.0, "units", "Pallet E-01, Gurgaon Hub"},
                {"Cisco Catalyst 9300 48-Port PoE+ Power Supply 715W", "NET-CIS-PWR-715W", "Networking & Optics", 7, 0, 5, 590.0, "units", "Rack N-01, Delhi Hub"},
                {"Wilo Submersible Drainage Pump 1.5kW 400V", "PMP-WILO-DRAIN-15", "HVAC Valves & Pumps", 8, 2, 5, 780.0, "units", "Pallet P-04, Gurgaon Hub"},
                {"Armstrong Steam Trap Stainless Steel Inverted Bucket", "VAL-ARM-STEAM-TRP", "HVAC Valves & Pumps", 14, 0, 10, 210.0, "units", "Bin V-05, Jaipur Hub"},
                {"SKF 6208 Deep Groove Precision Ball Bearing", "BRG-SKF-6208-2RS", "Mechanical Hardware", 60, 12, 25, 28.0, "units", "Bin M-08, Gurgaon Hub"},
                {"Telemecanique Emergency Stop Pushbutton IP65", "ELEC-TEL-ESTOP-65", "Industrial Controllers", 45, 6, 20, 36.0, "units", "Bin S-02, Noida Hub"},
                {"Kromschroder Gas Solenoid Safety Valve 2-Inch", "VAL-KROM-GAS-2IN", "Valves & Piping", 5, 1, 6, 840.0, "units", "Shelf G-02, Delhi Hub"},
                {"Phoenix Contact 24V 20A Power Supply Quint 4", "PWR-PHX-QUINT-24V", "Industrial Controllers", 18, 3, 10, 310.0, "units", "Cabinet C-03, Noida Hub"},
                {"Flir E8-XT Infrared Thermal Camera Calibrated", "TOOL-FLIR-E8XT", "Technician Tools", 4, 1, 4, 2850.0, "units", "Secure Vault, Central"},
                {"3M High Voltage Cold Shrink Termination Kit 33kV", "CAB-3M-TERM-33KV", "Cabling & Passive", 12, 2, 8, 420.0, "kits", "Rack C-08, Jaipur Hub"},
                {"Dwyer Magnehelic Differential Pressure Gauge 0-500Pa", "GAU-DWY-MAG-500", "Sensors & Detectors", 22, 4, 12, 115.0, "units", "Bin S-16, Delhi Hub"},
                {"Legrand 19-Inch 42U Server Rack Cable Manager", "RACK-LEG-42U-MGR", "Networking & Optics", 15, 0, 8, 75.0, "units", "Rack R-02, Noida Hub"},
                {"Fuchs Titan 15W-40 Heavy Duty Engine Oil 20L", "LUB-FUCH-15W40-20L", "Chemicals & Gases", 16, 4, 10, 95.0, "cans", "Hazmat Bay L-01, Chandigarh"},
                {"Siemens Desigo Room Automation Thermostat Touch", "BMS-SIEM-RDG-TOUCH", "Building Automation", 24, 6, 15, 180.0, "units", "Bin B-03, Gurgaon Hub"},
                {"Parker High Pressure Hydraulic Hose 2SN DN10 50m", "HYD-PARK-2SN-DN10", "Mechanical Hardware", 8, 1, 5, 290.0, "rolls", "Rack H-09, Delhi Hub"},
                {"Mitsubishi Heavy Industrial Compressor Contactor 150A", "ELEC-MHI-CON-150A", "Electrical Switchgear", 11, 2, 8, 160.0, "units", "Bin E-22, Gurgaon Hub"},
                {"Testo 550s Smart Digital Manifold with Bluetooth", "TOOL-TESTO-550S", "Technician Tools", 7, 1, 6, 520.0, "units", "Locker T-02, Central"},
                {"Enphase IQ8+ Microinverter 300W", "SOL-ENPH-IQ8PLUS", "Solar Components", 30, 8, 20, 195.0, "units", "Shelf B-02, Jaipur Hub"}
        };

        for (Object[] row : items) {
            int avail = (Integer) row[3];
            int reserved = (Integer) row[4];
            int min = (Integer) row[5];

            InventoryItemStatus status = (avail <= 0) ? InventoryItemStatus.OUT_OF_STOCK :
                    (avail <= min) ? InventoryItemStatus.LOW_STOCK :
                            InventoryItemStatus.IN_STOCK;

            InventoryItem item = InventoryItem.builder()
                    .name((String) row[0])
                    .sku((String) row[1])
                    .category((String) row[2])
                    .availableQuantity(avail)
                    .reservedQuantity(reserved)
                    .minThreshold(min)
                    .unitPrice((Double) row[6])
                    .unitOfMeasure((String) row[7])
                    .status(status)
                    .warehouseLocation((String) row[8])
                    .build();

            inventoryItemRepository.save(item);
        }
    }

    private void seedNotifications() {
        notificationRepository.save(Notification.builder()
                .title("CRITICAL SLA BREACH RISK")
                .message("Work Order WO-1042 (Fortis Healthcare) has an 82% SLA breach probability. Technician ETA is 21 minutes with 25 minutes remaining.")
                .severity(NotificationSeverity.CRITICAL)
                .readStatus(false)
                .relatedEntityType("WORK_ORDER")
                .relatedEntityId(1L)
                .build());

        notificationRepository.save(Notification.builder()
                .title("High SLA Risk Warning")
                .message("Work Order WO-1047 (Vertex Logistics) SLA breach probability elevated to 67%. Thermal hotspot requires immediate de-energization.")
                .severity(NotificationSeverity.WARNING)
                .readStatus(false)
                .relatedEntityType("WORK_ORDER")
                .relatedEntityId(2L)
                .build());

        notificationRepository.save(Notification.builder()
                .title("Low Inventory Stock Alert")
                .message("10Gbps SFP+ Optical Transceiver stock has fallen to 8 units (Threshold: 15 units). Restock reorder recommended.")
                .severity(NotificationSeverity.WARNING)
                .readStatus(false)
                .relatedEntityType("INVENTORY")
                .relatedEntityId(1L)
                .build());

        notificationRepository.save(Notification.builder()
                .title("Technician Dispatched")
                .message("Rahul Sharma was assigned and dispatched to WO-1042 with specialized VFD testing kit.")
                .severity(NotificationSeverity.INFO)
                .readStatus(true)
                .relatedEntityType("TECHNICIAN")
                .relatedEntityId(1L)
                .build());

        notificationRepository.save(Notification.builder()
                .title("Work Order Completed")
                .message("WO-1039 (Fiber Optic Splice Verification) successfully closed out with 100% SLA compliance.")
                .severity(NotificationSeverity.SUCCESS)
                .readStatus(true)
                .relatedEntityType("WORK_ORDER")
                .relatedEntityId(3L)
                .build());
    }
}
