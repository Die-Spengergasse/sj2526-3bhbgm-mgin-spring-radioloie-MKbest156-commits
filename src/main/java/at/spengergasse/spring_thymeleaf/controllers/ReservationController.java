package at.spengergasse.spring_thymeleaf.controllers;

import at.spengergasse.spring_thymeleaf.entities.BodyRegion;
import at.spengergasse.spring_thymeleaf.entities.Device;
import at.spengergasse.spring_thymeleaf.entities.DeviceRepository;
import at.spengergasse.spring_thymeleaf.entities.Patient;
import at.spengergasse.spring_thymeleaf.entities.PatientRepository;
import at.spengergasse.spring_thymeleaf.entities.Reservation;
import at.spengergasse.spring_thymeleaf.entities.ReservationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/reservation")
public class ReservationController {
    private final ReservationRepository reservationRepository;
    private final PatientRepository patientRepository;
    private final DeviceRepository deviceRepository;

    public ReservationController(
            ReservationRepository reservationRepository,
            PatientRepository patientRepository,
            DeviceRepository deviceRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.patientRepository = patientRepository;
        this.deviceRepository = deviceRepository;
    }

    @GetMapping("/add")
    public String addReservation(Model model) {
        model.addAttribute("reservation", new ReservationForm());
        model.addAttribute("patients", patientRepository.findAll());
        model.addAttribute("devices", deviceRepository.findAll());
        model.addAttribute("bodyRegions", BodyRegion.values());
        return "add_reservation";
    }

    @PostMapping("/add")
    public String addReservation(@ModelAttribute("reservation") ReservationForm reservationForm) {
        Patient patient = patientRepository.findById(reservationForm.getPatientId()).orElseThrow();
        Device device = deviceRepository.findById(reservationForm.getDeviceId()).orElseThrow();

        Reservation reservation = new Reservation();
        reservation.setPatient(patient);
        reservation.setDevice(device);
        reservation.setStartTime(reservationForm.getStartTime());
        reservation.setEndTime(reservationForm.getEndTime());
        reservation.setBodyRegion(reservationForm.getBodyRegion());
        reservation.setComment(reservationForm.getComment());
        reservationRepository.save(reservation);
        return "redirect:/reservation/list?deviceId=" + device.getId();
    }

    @GetMapping("/list")
    public String reservationList(@RequestParam(name = "deviceId", required = false) String deviceId, Model model) {
        List<Reservation> reservations = deviceId == null || deviceId.isBlank()
                ? List.of()
                : reservationRepository.findByDeviceIdOrderByStartTimeAsc(deviceId);

        model.addAttribute("devices", deviceRepository.findAll());
        model.addAttribute("selectedDeviceId", deviceId);
        model.addAttribute("reservations", reservations);
        return "reservation_list";
    }
}
