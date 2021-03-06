package systemtests;

import static org.junit.Assert.assertTrue;
import static seedu.budgeteer.commons.core.Messages.MESSAGE_INVALID_ENTRY_DISPLAYED_INDEX;
import static seedu.budgeteer.commons.core.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.budgeteer.logic.commands.DeleteCommand.MESSAGE_DELETE_ENTRY_SUCCESS;
import static seedu.budgeteer.testutil.TestUtil.getEntry;
import static seedu.budgeteer.testutil.TestUtil.getLastIndex;
import static seedu.budgeteer.testutil.TestUtil.getMidIndex;
import static seedu.budgeteer.testutil.TypicalEntrys.KEYWORD_MATCHING_BURSARY;
import static seedu.budgeteer.testutil.TypicalIndexes.INDEX_FIRST_ENTRY;

import org.junit.Test;

import seedu.budgeteer.commons.core.Messages;
import seedu.budgeteer.commons.core.index.Index;
import seedu.budgeteer.logic.commands.DeleteCommand;
import seedu.budgeteer.logic.commands.RedoCommand;
import seedu.budgeteer.logic.commands.UndoCommand;
import seedu.budgeteer.model.Model;
import seedu.budgeteer.model.entry.Entry;

public class DeleteCommandSystemTest extends EntriesBookSystemTest {

    private static final String MESSAGE_INVALID_DELETE_COMMAND_FORMAT =
            String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE);

    @Test
    public void delete() {
        /* ----------------- Performing delete operation while an unfiltered list is being shown -------------------- */

        /* Case: delete the first entry in the list, command with leading spaces and trailing spaces -> deleted */
        Model expectedModel = getModel();
        String command = "     " + DeleteCommand.COMMAND_WORD + "      " + INDEX_FIRST_ENTRY.getOneBased() + "       ";
        Entry deletedEntry = removeEntry(expectedModel, INDEX_FIRST_ENTRY);
        String expectedResultMessage = String.format(MESSAGE_DELETE_ENTRY_SUCCESS, deletedEntry);
        assertCommandSuccess(command, expectedModel, expectedResultMessage);

        /* Case: delete the last entry in the list -> deleted */
        Model modelBeforeDeletingLast = getModel();
        Index lastEntryIndex = getLastIndex(modelBeforeDeletingLast);
        assertCommandSuccess(lastEntryIndex);

        /* Case: undo deleting the last entry in the list -> last entry restored */
        command = UndoCommand.COMMAND_WORD;
        expectedResultMessage = UndoCommand.MESSAGE_SUCCESS;
        assertCommandSuccess(command, modelBeforeDeletingLast, expectedResultMessage);

        /* Case: redo deleting the last entry in the list -> last entry deleted again */
        command = RedoCommand.COMMAND_WORD;
        removeEntry(modelBeforeDeletingLast, lastEntryIndex);
        expectedResultMessage = RedoCommand.MESSAGE_SUCCESS;
        assertCommandSuccess(command, modelBeforeDeletingLast, expectedResultMessage);

        /* Case: delete the middle entry in the list -> deleted */
        Index middleEntryIndex = getMidIndex(getModel());
        assertCommandSuccess(middleEntryIndex);

        /* ------------------ Performing delete operation while a filtered list is being shown ---------------------- */

        /* Case: filtered entry list, delete index within bounds of budgeteer book and entry list -> deleted */
        showEntrysWithName(KEYWORD_MATCHING_BURSARY);
        Index index = INDEX_FIRST_ENTRY;
        assertTrue(index.getZeroBased() < getModel().getFilteredEntryList().size()); //TODO: FIX ASSERTION
        assertCommandSuccess(index);

        /* Case: filtered entry list, delete index within bounds of budgeteer book but out of bounds of entry list
         * -> rejected
         */
        showEntrysWithName(KEYWORD_MATCHING_BURSARY);
        int invalidIndex = getModel().getAddressBook().getEntryList().size();
        command = DeleteCommand.COMMAND_WORD + " " + invalidIndex;
        assertCommandFailure(command, MESSAGE_INVALID_ENTRY_DISPLAYED_INDEX);

        /* --------------------- Performing delete operation while a entry card is selected ------------------------ */

        /* Case: delete the selected entry -> entry list panel selects the entry before the deleted entry */
        showAllEntrys();
        expectedModel = getModel();
        Index selectedIndex = getLastIndex(expectedModel);
        Index expectedIndex = Index.fromZeroBased(selectedIndex.getZeroBased() - 1);
        selectEntry(selectedIndex);
        command = DeleteCommand.COMMAND_WORD + " " + selectedIndex.getOneBased();
        deletedEntry = removeEntry(expectedModel, selectedIndex);
        expectedResultMessage = String.format(MESSAGE_DELETE_ENTRY_SUCCESS, deletedEntry);
        assertCommandSuccess(command, expectedModel, expectedResultMessage, expectedIndex);

        /* --------------------------------- Performing invalid delete operation ------------------------------------ */

        /* Case: invalid index (0) -> rejected */
        command = DeleteCommand.COMMAND_WORD + " 0";
        assertCommandFailure(command, MESSAGE_INVALID_DELETE_COMMAND_FORMAT);

        /* Case: invalid index (-1) -> rejected */
        command = DeleteCommand.COMMAND_WORD + " -1";
        assertCommandFailure(command, MESSAGE_INVALID_DELETE_COMMAND_FORMAT);

        /* Case: invalid index (size + 1) -> rejected */
        Index outOfBoundsIndex = Index.fromOneBased(
                getModel().getAddressBook().getEntryList().size() + 1);
        command = DeleteCommand.COMMAND_WORD + " " + outOfBoundsIndex.getOneBased();
        assertCommandFailure(command, MESSAGE_INVALID_ENTRY_DISPLAYED_INDEX);

        /* Case: invalid arguments (alphabets) -> rejected */
        assertCommandFailure(DeleteCommand.COMMAND_WORD + " abc", MESSAGE_INVALID_DELETE_COMMAND_FORMAT);

        /* Case: invalid arguments (extra argument) -> rejected */
        assertCommandFailure(DeleteCommand.COMMAND_WORD + " 1 abc", MESSAGE_INVALID_DELETE_COMMAND_FORMAT);

        /* Case: mixed case command word -> rejected */
        assertCommandFailure("DelETE 1", MESSAGE_UNKNOWN_COMMAND);
    }

    /**
     * Removes the {@code Entry} at the specified {@code index} in {@code model}'s budgeteer book.
     * @return the removed entry
     */
    private Entry removeEntry(Model model, Index index) {
        Entry targetEntry = getEntry(model, index);
        model.deleteEntry(targetEntry);
        return targetEntry;
    }

    /**
     * Deletes the entry at {@code toDelete} by creating a default {@code DeleteCommand} using {@code toDelete} and
     * performs the same verification as {@code assertCommandSuccess(String, Model, String)}.
     * @see DeleteCommandSystemTest#assertCommandSuccess(String, Model, String)
     */
    private void assertCommandSuccess(Index toDelete) {
        Model expectedModel = getModel();
        Entry deletedEntry = removeEntry(expectedModel, toDelete);
        String expectedResultMessage = String.format(MESSAGE_DELETE_ENTRY_SUCCESS, deletedEntry);

        assertCommandSuccess(
                DeleteCommand.COMMAND_WORD + " " + toDelete.getOneBased(), expectedModel, expectedResultMessage);
    }

    /**
     * Executes {@code command} and in addition,<br>
     * 1. Asserts that the command box displays an empty string.<br>
     * 2. Asserts that the result display box displays {@code expectedResultMessage}.<br>
     * 3. Asserts that the browser url and selected card remains unchanged.<br>
     * 4. Asserts that the status bar's sync status changes.<br>
     * 5. Asserts that the command box has the default style class.<br>
     * Verifications 1 and 2 are performed by
     * {@code EntriesBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)}.
     * @see EntriesBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)
     */
    private void assertCommandSuccess(String command, Model expectedModel, String expectedResultMessage) {
        assertCommandSuccess(command, expectedModel, expectedResultMessage, null);
    }

    /**
     * Performs the same verification as {@code assertCommandSuccess(String, Model, String)} except that the browser url
     * and selected card are expected to update accordingly depending on the card at {@code expectedSelectedCardIndex}.
     * @see DeleteCommandSystemTest#assertCommandSuccess(String, Model, String)
     * @see EntriesBookSystemTest#assertSelectedCardChanged(Index)
     */
    private void assertCommandSuccess(String command, Model expectedModel, String expectedResultMessage,
            Index expectedSelectedCardIndex) {
        executeCommand(command);
        assertApplicationDisplaysExpected("", expectedResultMessage, expectedModel);

        if (expectedSelectedCardIndex != null) {
            assertSelectedCardChanged(expectedSelectedCardIndex);
        } else {
            assertSelectedCardUnchanged();
        }

        assertCommandBoxShowsDefaultStyle();
        assertStatusBarUnchangedExceptSyncStatus();
    }

    /**
     * Executes {@code command} and in addition,<br>
     * 1. Asserts that the command box displays {@code command}.<br>
     * 2. Asserts that result display box displays {@code expectedResultMessage}.<br>
     * 3. Asserts that the browser url, selected card and status bar remain unchanged.<br>
     * 4. Asserts that the command box has the error style.<br>
     * Verifications 1 and 2 are performed by
     * {@code EntriesBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)}.<br>
     * @see EntriesBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)
     */
    private void assertCommandFailure(String command, String expectedResultMessage) {
        Model expectedModel = getModel();

        executeCommand(command);
        assertApplicationDisplaysExpected(command, expectedResultMessage, expectedModel);
        assertSelectedCardUnchanged();
        assertCommandBoxShowsErrorStyle();
        assertStatusBarUnchanged();
    }
}
