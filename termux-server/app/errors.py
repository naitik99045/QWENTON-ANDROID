from fastapi import HTTPException, status

class PolicyViolationError(HTTPException):
    def __init__(self, detail: str):
        super().__init__(status_code=status.HTTP_403_FORBIDDEN, detail=detail)

class ToolNotFoundError(HTTPException):
    def __init__(self, tool_name: str):
        super().__init__(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Tool '{tool_name}' is not registered or allowed."
        )

class CommandExpiredError(HTTPException):
    def __init__(self, command_id: str):
        super().__init__(
            status_code=status.HTTP_410_GONE,
            detail=f"Command proposal '{command_id}' has expired."
        )

class IntegrityMismatchError(HTTPException):
    def __init__(self, detail: str = "Command integrity verification failed."):
        super().__init__(status_code=status.HTTP_400_BAD_REQUEST, detail=detail)
