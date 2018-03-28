package org.alixia.chatroom.api.pub;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public final class DeclaredValueChecker {

	public DeclaredValueChecker() {
	}

	/**
	 * Checks if the current value of a variable is the same as the value it was
	 * declared as during class initialization. Rules dictating whether or not this
	 * method can check for the default value of a given variable are as follows:
	 * <ol>
	 * <li>The variable must be static.</li>
	 * <li>The variable must have been initialized to the value of a constant
	 * expression.</li>
	 * <li>The variable must be a primitive type.</li>
	 * </ol>
	 * 
	 * @param varName
	 *            The name of the variable to be checked. This variable must be
	 *            static and belong to the {@link Class}, <code>clas</code>.
	 * @param clas
	 *            The class of which a static variable, named <code>varName</code>,
	 *            belongs.
	 * @return <code>true</code> if the specified variable's value as set in the
	 *         bytecode is the same as it is when retrieved reflectively at the
	 *         beginning of this method call, <code>false</code> otherwise.
	 * @throws UnsupportedOperationException
	 *             If the specified variable's type is not primitive.
	 * @throws SecurityException
	 *             As thrown by {@link Class#getDeclaredField(String)} and any other
	 *             reflective operation executed by this method.
	 * @throws NoSuchFieldException
	 *             If attempting to access the field with the given arguments to
	 *             this method, throws a {@link NoSuchFieldException}.
	 * @throws IllegalArgumentException
	 *             In case the <code>varName</code> parameter does refer to a field
	 *             of the given class, but the field is not static.
	 * @throws RuntimeException
	 *             In case an unknown or unexpected error occurs.
	 * @throws IOException
	 *             If an IOException occurs while reading from the
	 *             <code>.class</code> file of the specified class.
	 */
	public static boolean isOriginalValue(String varName, Class<?> clas) throws UnsupportedOperationException,
			NoSuchFieldException, SecurityException, IllegalArgumentException, RuntimeException, IOException {
		Object currentValue;

		// Check for the current value reflectively:
		Field field = clas.getDeclaredField(varName);
		if (!field.getType().isPrimitive())
			throw new UnsupportedOperationException("The specified variable's type is not primitive.");
		field.setAccessible(true);
		try {
			// We pass in null because we assume that the field is static. If it isn't, an
			// IllegalArgumentException is thrown.
			currentValue = field.get(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("The current value of the variable could not be accessed.", e);
		}

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		// Map used to store constant pool values:
		Map<Integer, Object> constants = new HashMap<>();

		// Local classes used to represent certain constant pool items:
		class CPoolClass {
			public CPoolClass(int internalNameIndex) {
			}

		}
		class CPoolNameAndType {
			public final int nameIndex;

			public CPoolNameAndType(int nameIndex, int typeIndex) {
				this.nameIndex = nameIndex;
			}

			public String getName() {
				return (String) constants.get(nameIndex);
			}

		}
		class CPoolField {
			public final int nameAndTypeIndex;

			public CPoolField(int classIndex, int nameAndTypeIndex) {
				this.nameAndTypeIndex = nameAndTypeIndex;
			}

			public CPoolNameAndType getNameAndType() {
				return (CPoolNameAndType) constants.get(nameAndTypeIndex);
			}
		}
		class CPoolString {
			public CPoolString(int stringIndex) {
			}
		}

		// Now to read the bytecode of the class for the original value.
		// First, we open a stream to the .class file of clas.
		DataInputStream reader = new DataInputStream(clas.getResourceAsStream(clas.getSimpleName() + ".class"));

		// A .class file starts with the magic hexadecimal code: 0xCafeBabe.
		if (reader.readInt() != 0xCafeBabe)
			throw new RuntimeException("Failed to parse the given class's .class file header.");
		// Next, the file contains four bytes denoting its version.
		reader.readInt();// Read and ditch the four bytes; we don't need them.

		// Parse the constant pool size (2 bytes). We'll go through this many items in
		// the constant pool, and cache the ones we need.
		int constantPoolSize = reader.readShort();

		// Constant pool is of size constantPoolSize-1, so we iterate from 1 to the
		// size.
		for (int i = 1; i < constantPoolSize; i++) {
			byte tag = reader.readByte();
			switch (tag) {
			default:
				throw new RuntimeException(
						"Could not parse the .class file's constant pool; couldn't determine the initial value of the variable.");
			case 1:// String tag
				constants.put(i, reader.readUTF());
				continue;

			case 3:// Int tag
				constants.put(i, reader.readInt());
				continue;

			case 4:// Float tag
				constants.put(i, reader.readFloat());
				continue;

			case 5:// Long tag
				constants.put(i, reader.readLong());
				i++;
				continue;

			case 6:// Double tag
				constants.put(i, reader.readDouble());
				i++;
				continue;

			case 7:// Class item
				constants.put(i, new CPoolClass(reader.readShort()));
				continue;

			case 8:// Literal String (e.g. "abc", "potato", "a string")
				constants.put(i, new CPoolString(reader.readShort()));
				continue;

			case 9:// Field item
				constants.put(i, new CPoolField(reader.readShort(), reader.readShort()));
				continue;

			case 10:// Method (We don't need to cache this)
			case 11:// Abstract method
				reader.readInt();
				continue;

			case 12:// NameAndType item
				constants.put(i, new CPoolNameAndType(reader.readShort(), reader.readShort()));
				continue;

			case 15:// Method Handle
				reader.readByte();
				reader.readShort();
				continue;

			case 16:// Method Type
				reader.readShort();// Skip 2 bytes
				continue;

			case 18:// Invoke Dynamic
				reader.readInt();
				continue;
			}
		}

		// After the constant pool, there are a few, 2 byte-sized values, a list of
		// interfaces (that the class implements), a list of fields, then the list of
		// methods.
		//
		// We're gonna skip to the methods.

		reader.readInt();// Skip 4 bytes
		reader.readShort();// Skip 2 bytes

		int interfaceListSize = reader.readShort();
		for (int i = 0; i < interfaceListSize; i++)
			reader.readShort();

		int fieldListSize = reader.readShort();
		for (int i = 0; i < fieldListSize; i++) {

			// Skip 6 bytes (total)
			reader.readShort();
			reader.readInt();

			int attributeCount = reader.readShort();
			// Skip through all the attributes.
			for (int j = 0; j < attributeCount; j++) {
				reader.readShort();
				int attributeSize = reader.readInt();
				for (int k = 0; k < attributeSize; k++)
					reader.readByte();
			}

		}

		int methodCount = reader.readShort();

		for (int i = 0; i < methodCount; i++) {

			// Skip the method's modifiers.
			reader.readShort();

			// Static variables are initialized via the <clinit> method, whether or not they
			// are placed in static initialization blocks in the source code. Because of
			// this, we'll have to check the code in this method.
			boolean isClinit = "<clinit>".equals(constants.get((int) reader.readShort()));

			reader.readShort();// Skip over the method descriptor.

			short attributeCount = reader.readShort();

			if (isClinit) {
				// This is the method we want. Iterate over each attribute it has.
				for (int j = 0; j < attributeCount; j++) {
					if ("Code".equals(constants.get((int) reader.readShort()))) {

						reader.readInt();// Skip over the attribute size. We only need the code table size.
						reader.readInt();// Skip max stack size and max locals.

						int codeLength = reader.readInt();

						// Doubles and longs in the stack will have a null value after them. This
						// emulates the numbering of an actual stack.
						Stack<Object> stack = new Stack<Object>() {

							private static final long serialVersionUID = 1L;

							// Handles the push method
							@Override
							public void addElement(Object item) {
								super.addElement(item);
								if (item instanceof Double || item instanceof Long)
									super.addElement(null);
							}

							// Handles the add method
							@Override
							public synchronized void insertElementAt(Object obj, int index) {
								if (obj instanceof Double || obj instanceof Long)
									super.insertElementAt(null, index);
								super.insertElementAt(obj, index);
							}

							@Override
							public synchronized Object pop() {
								Object item = super.pop();
								if (item == null)
									return super.pop();
								return item;
							}

							@Override
							public synchronized Object remove(int index) {
								Object item = super.remove(index);
								if (item == null)
									item = super.remove(index - 1);
								else if (item instanceof Double || item instanceof Long)
									super.remove(index);
								return item;
							}

							@Override
							public synchronized Object peek() {
								Object item = super.peek();
								if (item == null)
									return super.get(size() - 2);
								return item;
							}

						};

						int[] bytes = new int[codeLength];
						for (int k = 0; k < codeLength; k++)
							bytes[k] = reader.readUnsignedByte();

						// Stack<Object> locals = new Stack<>();
						// If I supported local vars, I'd feel obligated to support arrays here, as
						// well, and, right now, I don't have enough time for that. Feel free to edit
						// this question. Otherwise, I may edit this method to support more opcodes
						// later.

						Object variable = null;
						boolean set = false;

						// Here is where we simulate the class's initialization. This process doesn't
						// handle things like arrays, method calls, or other variables.
						for (int k = 0; k < codeLength; k++) {
							// When parsing through mnemonics, we ignore anything that only puts a reference
							// onto the stack or removes a reference from the stack, since this method only
							// supports primitive types.

							switch (bytes[k]) {

							default:
								continue;

							// Pushing

							case 0x10:// bipush
								stack.push(bytes[++k]);
								break;
							case 0x11:// sipush
								stack.push((bytes[++k] << 8) + bytes[++k]);
								break;
							case 0x12:// ldc
							case 0x14:// ldc2_w
								Object item = constants.get((bytes[++k] << 8) + bytes[++k]);
								if (item instanceof Integer || item instanceof Float || item instanceof Long
										|| item instanceof Double)
									stack.push(item);
								break;

							// Doubles

							case 0xe:// dconst_0
								stack.push((double) 0);
								break;
							case 0xf:// dconst_1
								stack.push((double) 1);
								break;

							case 0x63:// dadd
								stack.push((double) stack.pop() + (double) stack.pop());
								break;
							case 0x6f:// ddiv
								stack.push((double) stack.pop() / (double) stack.pop());
								break;
							case 0x6b:// dmul
								stack.push((double) stack.pop() * (double) stack.pop());
								break;
							case 0x77:// dneg
								stack.push(-(double) stack.pop());
								break;
							case 0x73:// drem
								stack.push((double) stack.pop() % (double) stack.pop());
								break;
							case 0x67:// dsub
								stack.push((double) stack.pop() - (double) stack.pop());
								break;

							case 0x59:// dup
								stack.push(stack.peek());
								break;
							case 0x5a:// dup_x1
							case 0x5d:// dup2_x1
								stack.add(stack.size() - 2, stack.peek());
								break;
							case 0x5b:// dup_x2
							case 0x5e:// dup2_x2
								stack.add(stack.size() - 3, stack.peek());
								break;

							// Floats

							case 0xb:// fconst_0
								stack.push(0f);
								break;
							case 0xc:// fconst_1
								stack.push(1f);
								break;
							case 0xd:// fconst_2
								stack.push(2f);
								break;

							case 0x62:// fadd
								stack.push((float) stack.pop() + (float) stack.pop());
								break;
							case 0x6e:// fdiv
								stack.push((float) stack.pop() / (float) stack.pop());
								break;
							case 0x6a:// fmul
								stack.push((float) stack.pop() * (float) stack.pop());
								break;
							case 0x76:// fneg
								stack.push(-(float) stack.pop());
								break;
							case 0x72:// frem
								stack.push((float) stack.pop() % (float) stack.pop());
								break;
							case 0x66:// fsub
								stack.push((float) stack.pop() - (float) stack.pop());
								break;

							// Integers

							case 0x2:// iconst_m1
								stack.push(-1);
								break;
							case 0x3:// iconst_0
								stack.push(0);
								break;
							case 0x4:// iconst1
								stack.push(1);
								break;
							case 0x5:// iconst_2
								stack.push(2);
								break;
							case 0x6:// iconst_3
								stack.push(3);
								break;
							case 0x7:// iconst_4
								stack.push(4);
								break;
							case 0x8:// iconst_5
								stack.push(5);
								break;

							case 0x60:// iadd
								stack.push((int) stack.pop() + (int) stack.pop());
								break;
							case 0x7e:// iand
								stack.push((int) stack.pop() & (int) stack.pop());
								break;
							case 0x6c:// idiv
								stack.push((int) stack.pop() / (int) stack.pop());
								break;
							case 0x68:// imul
								stack.push((int) stack.pop() * (int) stack.pop());
								break;
							case 0x74:// ineg
								stack.push(-(int) stack.pop());
								break;
							case 0x80:// ior
								stack.push((int) stack.pop() | (int) stack.pop());
								break;
							case 0x70:// irem
								stack.push((int) stack.pop() % (int) stack.pop());
								break;
							case 0x78:// ishl
								stack.push((int) stack.pop() << (int) stack.pop());
								break;
							case 0x7a:// ishr
								stack.push((int) stack.pop() >> (int) stack.pop());
								break;
							case 0x64:// isub
								stack.push((int) stack.pop() - (int) stack.pop());
								break;
							case 0x7c:// iushr
								stack.push((int) stack.pop() >>> (int) stack.pop());
								break;
							case 0x82:// ixor
								stack.push((int) stack.pop() ^ (int) stack.pop());
								break;

							// Longs

							case 0x9:// lconst_0
								stack.push(0l);
								break;
							case 0xa:// lconst_1
								stack.push(1l);
								break;

							case 0x61:// ladd
								stack.push((long) stack.pop() + (long) stack.pop());
								break;
							case 0x7f:// land
								stack.push((long) stack.pop() & (long) stack.pop());
								break;
							case 0x6d:// ldiv
								stack.push((long) stack.pop() / (long) stack.pop());
								break;
							case 0x69:// lmul
								stack.push((long) stack.pop() * (long) stack.pop());
								break;
							case 0x75:// lneg
								stack.push(-(long) stack.pop());
								break;
							case 0x81:// lor
								stack.push((long) stack.pop() | (long) stack.pop());
								break;
							case 0x71:// lrem
								stack.push((long) stack.pop() % (long) stack.pop());
								break;
							case 0x79:// lshl
								stack.push((long) stack.pop() << (long) stack.pop());
								break;
							case 0x7b:// lshr
								stack.push((long) stack.pop() >> (long) stack.pop());
								break;
							case 0x65:// lsub
								stack.push((long) stack.pop() - (long) stack.pop());
								break;
							case 0x7d:// lushr
								stack.push((long) stack.pop() >>> (long) stack.pop());
								break;
							case 0x83:// lxor
								stack.push((long) stack.pop() ^ (long) stack.pop());
								break;

							// Stack manipulation

							// nop is handled by this switch's default case.
							case 0x58:// pop2
								stack.pop();
							case 0x57:// pop
								stack.pop();
								break;
							case 0x5f:// swap
								Object top = stack.pop(), bottom = stack.pop();
								stack.push(top);
								stack.push(bottom);
								break;

							// Variable assignment

							case 0xb3:// putstatic

								if (varName.equals(((CPoolField) constants.get((bytes[++k] << 8) + bytes[++k]))
										.getNameAndType().getName())) {
									variable = stack.pop();
									set = true;
								}
								break;

							// Casting

							case 0x90:// d2f
								stack.push((float) (double) stack.pop());
								break;
							case 0x8e:// d2i
								stack.push((int) (double) stack.pop());
								break;
							case 0x8f:// dtl
								stack.push((long) (double) stack.pop());
								break;

							case 0x91:// i2b
								stack.push((byte) (int) stack.pop());
								break;
							case 0x92:// i2c
								stack.push((char) (int) stack.pop());
								break;
							case 0x87:// i2d
								stack.push((double) (int) stack.pop());
								break;
							case 0x86:// i2f
								stack.push((float) (int) stack.pop());
								break;
							case 0x85:// i2l
								stack.push((long) (int) stack.pop());
								break;
							case 0x93:// i2s
								stack.push((short) (int) stack.pop());
								break;

							case 0x8a:// l2d
								stack.push((double) (long) stack.pop());
								break;
							case 0x89:// l2f
								stack.push((float) (long) stack.pop());
								break;
							case 0x88:// l2i
								stack.push((int) (long) stack.pop());
								break;

							case 0x8d:// f2d
								stack.push((double) (float) stack.pop());
								break;
							case 0x8b:// f2i
								stack.push((int) (float) stack.pop());
								break;
							case 0x8c:// ftl
								stack.push((long) (float) stack.pop());
								break;

							// Comparisons

							case 0x98:// dcmpg
								stack.push((double) stack.pop() > (double) stack.pop());
								break;
							case 0x96:// fcmpg
								stack.push((float) stack.pop() > (float) stack.pop());
								break;

							case 0x97:// dcmpl
								stack.push((double) stack.pop() < (double) stack.pop());
								break;
							case 0x95:// fcmpl
								stack.push((float) stack.pop() < (float) stack.pop());
								break;

							case 0x94:// lcmp
								stack.push(((Long) stack.pop()).compareTo((Long) stack.pop()));
								break;

							}
						}
						// Set will be false here if there is initialization code for the class
						// (something happens during class init) but our variable isn't initialized.
						return !set ? ((Object) false).equals(currentValue) || ((Object) 0).equals(currentValue)
								: variable.equals(currentValue);

					} else
						// This isn't the "Code" attribute. Skip it.
						for (int k = 0; k < reader.readInt(); k++)
							reader.readByte();

				}
				throw new RuntimeException(
						"Couldn't find the static initialization bytecode for the specified class. Any initial assignments or declarations to the specified variable could not be found.");
			} else {
				// We don't want this method.
				for (int j = 0; j < attributeCount; j++) {
					reader.readShort();// Skip the attribute name. We don't care for this method.
					for (int k = 0; k < reader.readInt(); k++)
						reader.readByte();// Skip the attribute data.
				}
			}
		}

		// We get to here if there is no <clinit> method because there is no code to run
		// during class initialization. Compare if the variable is its default value.
		return ((Object) false).equals(currentValue) || ((Object) 0).equals(currentValue);
	}

}
