/**
 * Copyright (c) 2007-2015 Regents of the University of California (Regents).
 * Created by WISE, Graduate School of Education, University of California, Berkeley.
 *
 * This software is distributed under the GNU General Public License, v3,
 * or (at your option) any later version.
 *
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 *
 * REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE SOFTWARE AND ACCOMPANYING DOCUMENTATION, IF ANY, PROVIDED
 * HEREUNDER IS PROVIDED "AS IS". REGENTS HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * REGENTS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.wise.portal.domain;

import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;

/**
 * A checked exception that gets thrown when the student that was to be
 * added to a <code>Run</code> was already-associated with the run.
 *
 * @author Hiroki Terashima
 */
public class StudentUserAlreadyAssociatedWithRunException extends Exception {

  private static final long serialVersionUID = 1L;

  public StudentUserAlreadyAssociatedWithRunException(String message) {
    super(message);
  }

  public StudentUserAlreadyAssociatedWithRunException(Throwable cause) {
    super(cause);
  }

  public StudentUserAlreadyAssociatedWithRunException(String message, Throwable cause) {
    super(message, cause);
  }

  public StudentUserAlreadyAssociatedWithRunException(User studentUser, Run run) {
    super("studentuser: " + studentUser +
      " is already associated with run: " + run);
  }
}
