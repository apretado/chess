package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;

public class ClearServiceTest {
    static final ClearService clearService = new ClearService(
        new MemoryUserDAO(), new MemoryAuthDAO(), new MemoryGameDAO()
    );
    
}
