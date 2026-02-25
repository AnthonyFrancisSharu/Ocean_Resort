package dao;

/**
 * Staff - Model class representing a staff member
 */
public class Staff {

    private String id;
    private String name;
    private String email;
    private String contact;
    private String role;
    private String dept;
    private String nic;
    private String username;
    private String password;
    private String status;

    public Staff() {}

    public Staff(String id, String name, String email, String contact,
                 String role, String dept, String nic,
                 String username, String password, String status) {
        this.id       = id;
        this.name     = name;
        this.email    = email;
        this.contact  = contact;
        this.role     = role;
        this.dept     = dept;
        this.nic      = nic;
        this.username = username;
        this.password = password;
        this.status   = status;
    }

    // ── Getters ──
    public String getId()       { return id; }
    public String getName()     { return name; }
    public String getEmail()    { return email; }
    public String getContact()  { return contact; }
    public String getRole()     { return role; }
    public String getDept()     { return dept; }
    public String getNic()      { return nic; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getStatus()   { return status; }

    // ── Setters ──
    public void setId(String id)           { this.id = id; }
    public void setName(String name)       { this.name = name; }
    public void setEmail(String email)     { this.email = email; }
    public void setContact(String contact) { this.contact = contact; }
    public void setRole(String role)       { this.role = role; }
    public void setDept(String dept)       { this.dept = dept; }
    public void setNic(String nic)         { this.nic = nic; }
    public void setUsername(String u)      { this.username = u; }
    public void setPassword(String p)      { this.password = p; }
    public void setStatus(String status)   { this.status = status; }

    /** Convert to JSON string for HTTP responses */
    public String toJson() {
        return "{" +
                "\"id\":\""       + id       + "\"," +
                "\"name\":\""     + name     + "\"," +
                "\"email\":\""    + email    + "\"," +
                "\"contact\":\""  + contact  + "\"," +
                "\"role\":\""     + role     + "\"," +
                "\"dept\":\""     + dept     + "\"," +
                "\"nic\":\""      + nic      + "\"," +
                "\"username\":\"" + username + "\"," +
                "\"status\":\""   + status   + "\"" +
                "}";
    }
}