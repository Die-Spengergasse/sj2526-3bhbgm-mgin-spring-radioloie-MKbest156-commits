package at.spengergasse.spring_thymeleaf.controllers;

import at.spengergasse.spring_thymeleaf.entities.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    // ================= EXCEPTIONS =================

    public static class DeviceAlreadyReservedException extends RuntimeException {
        public DeviceAlreadyReservedException(String message) {
            super(message);
        }
    }

    public static class PatientAlreadyReservedException extends RuntimeException {
        public PatientAlreadyReservedException(String message) {
            super(message);
        }
    }

    // ================= ADD FORM =================

    @GetMapping("/add")
    public String addReservation(Model model) {
        model.addAttribute("reservation", new ReservationForm());
        model.addAttribute("patients", patientRepository.findAll());
        model.addAttribute("devices", deviceRepository.findAll());
        model.addAttribute("bodyRegions", BodyRegion.values());
        return "add_reservation";
    }

    // ================= CREATE RESERVATION =================

    @PostMapping("/add")
    public String addReservation(@ModelAttribute("reservation") ReservationForm reservationForm) {

        if (reservationForm.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Ein Termin in der Vergangenheit kann nicht reserviert werden."
            );
        }

        // ===== Patient laden =====
        Patient patient = patientRepository.findById(reservationForm.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient nicht gefunden"));

        // ===== Device laden =====
        Device device = deviceRepository.findById(reservationForm.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Gerät nicht gefunden"));

        // ===== Patient prüfen =====
        boolean patientReserved = reservationRepository.isPatientReserved(
                patient.getId(),
                reservationForm.getStartTime(),
                reservationForm.getEndTime()
        );

        if (patientReserved) {
            throw new PatientAlreadyReservedException(
                    "Der Patient hat bereits einen Termin in diesem Zeitraum."
            );
        }

        // ===== Device prüfen =====
        boolean deviceReserved = reservationRepository.isDeviceReserved(
                device.getId(),
                reservationForm.getStartTime(),
                reservationForm.getEndTime()
        );

        if (deviceReserved) {
            throw new DeviceAlreadyReservedException(
                    "Das Gerät ist bereits in diesem Zeitraum reserviert."
            );
        }

        // ===== Reservation erstellen =====
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

    // ================= LIST =================

    @GetMapping("/list")
    public String reservationList(
            @RequestParam(name = "deviceId", required = false) String deviceId,
            Model model
    ) {

        List<Reservation> reservations = (deviceId == null || deviceId.isBlank())
                ? List.of()
                : reservationRepository.findByDeviceIdOrderByStartTimeAsc(deviceId);

        model.addAttribute("devices", deviceRepository.findAll());
        model.addAttribute("selectedDeviceId", deviceId);
        model.addAttribute("reservations", reservations);

        return "reservation_list";
    }

    // ================= EXCEPTION HANDLERS =================

    @ExceptionHandler(DeviceAlreadyReservedException.class)
    public String handleDeviceAlreadyReserved(
            DeviceAlreadyReservedException ex,
            Model model
    ) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(PatientAlreadyReservedException.class)
    public String handlePatientAlreadyReserved(
            PatientAlreadyReservedException ex,
            Model model
    ) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(
            IllegalArgumentException ex,
            Model model
    ) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }
}