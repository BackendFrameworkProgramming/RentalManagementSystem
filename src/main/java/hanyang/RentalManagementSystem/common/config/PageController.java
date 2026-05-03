package hanyang.RentalManagementSystem.common.config;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() { return "index"; }

    // 화면1: 디바이스 현황 (PM)
    @GetMapping("/devices")
    public String devices(Model model) { model.addAttribute("menu","devices"); model.addAttribute("pageTitle","디바이스 현황"); return "device/list"; }

    // 화면2: 임대 (팀원3)
    @GetMapping("/rentals")
    public String rentals(Model model) { model.addAttribute("menu","rentals"); model.addAttribute("pageTitle","디바이스 임대 현황"); return "rental/list"; }

    // 화면3: 생체정보 (팀원2)
    @GetMapping("/biometric")
    public String biometric(Model model) { model.addAttribute("menu","biometric"); model.addAttribute("pageTitle","디바이스 사용 생체 정보"); return "biometric/list"; }

    // 화면4: AS (팀원3)
    @GetMapping("/as-records")
    public String asRecords(Model model) { model.addAttribute("menu","as"); model.addAttribute("pageTitle","디바이스 AS 관리"); return "as-record/list"; }

    // 화면5: 지점 (팀원1)
    @GetMapping("/branches")
    public String branches(Model model) { model.addAttribute("menu","branches"); model.addAttribute("pageTitle","지점 관리"); return "branch/list"; }

    // 화면6: 센터정보 (PM)
    @GetMapping("/center")
    public String center(Model model) { model.addAttribute("menu","center"); model.addAttribute("pageTitle","센터정보"); return "center/form"; }

    // 화면7: 부서/팀 (팀원1)
    @GetMapping("/departments")
    public String departments(Model model) { model.addAttribute("menu","departments"); model.addAttribute("pageTitle","부서/팀"); return "department/list"; }

    // 화면8: 직원 (팀원2)
    @GetMapping("/employees")
    public String employees(Model model) { model.addAttribute("menu","employees"); model.addAttribute("pageTitle","센터 담당직원"); return "employee/list"; }

    // 에러로그 (PM)
    @GetMapping("/system-logs")
    public String systemLogs(Model model) { model.addAttribute("menu","logs"); model.addAttribute("pageTitle","에러로그"); return "system-log/list"; }
}
