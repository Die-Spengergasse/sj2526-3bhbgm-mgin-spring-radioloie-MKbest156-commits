package at.spengergasse.spring_thymeleaf.controllers;

import at.spengergasse.spring_thymeleaf.entities.Patient;
import at.spengergasse.spring_thymeleaf.entities.PatientRepository;
import at.spengergasse.spring_thymeleaf.entities.Gender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/patient")
public class PatientController {
    private final PatientRepository patientRepository;

    public PatientController(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @GetMapping("/list")
    public String patients(Model model) {
        model.addAttribute("patients", patientRepository.findAll());
        return "patlist";
    }

    @GetMapping("/add")
    public String addPatient(Model model) {
        model.addAttribute("patient", new Patient());
        model.addAttribute("genders", Gender.values());
        return "add_patient";
    }

    @PostMapping("/add")
    public String addPatient(@ModelAttribute("patient") Patient patient, Model model) {

        if (patient.getBirthDate() != null &&
                patient.getBirthDate().isAfter(java.time.LocalDate.now())) {

            model.addAttribute("error",
                    "Das Geburtsdatum darf nicht in der Zukunft liegen.");
            model.addAttribute("genders", Gender.values());
            return "add_patient";
        }

        String svnr = patient.getSocialInsuranceNumber();

        if (svnr == null || !svnr.matches("\\d{10}")) {
            model.addAttribute("error",
                    "Ungültige Sozialversicherungsnummer. Es müssen genau 10 Ziffern eingegeben werden.");
            model.addAttribute("genders", Gender.values());
            return "add_patient";
        }

        patientRepository.save(patient);
        return "redirect:/patient/list";
    }
}
